package relay;

import static repast.simphony.engine.schedule.ScheduleParameters.createRepeating;
import static repast.simphony.util.ContextUtils.getContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import configuration.Configuration;
import network.CustomEdge;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import styles.RelayStyle;
import waves.ProtocolWave;
import waves.RetransmissionProtocolWave;
import waves.RetransmissionRequestWave;
import waves.Wave;

public class Relay {

  /** Used as wave SourceID and as forwarderID */
  private int id;

  /** Space shared among neighbours */
  private ContinuousSpace<Object> space;

  /** Set of neighbours induced by Configuation.WAVE_PROPAGATION_DISTANCE */
  private Set<Relay> neighbours;

  /** Latency computed based on the distance between the relay and its neighbours */
  private Map<Relay, Integer> neighboursLatency;

  /** Reference that will be used for the next generated wave */
  private int currentReference;

  /** Map of SourceIDs (Relay.id) to expected next reference */
  private Map<Integer, Integer> frontier;

  /** Queue used to process received waves sorted by delivery time */
  private PriorityQueue<ProtocolWave> receivedWaves;

  /**
   * Map of SourceID to a sorted queue of waves that cannot be processed when received at the relay
   * due to missing messages
   */
  private Map<Integer, PriorityQueue<ProtocolWave>> outOfOrderWaves;


  /** Queue storing the waves sent by the relay in the last period */
  private Queue<Pair<Integer, ProtocolWave>> lastSentWaves;

  /** Append-only log per SourceID */
  private Map<Integer, List<ProtocolWave>> log;


  /** Network used to represent nodes and transmissions */
  private Network<Object> network;

  private RelayStyle.style style;

  // METRICS


  /** Number of messages received by the relay (per tick) */
  private int receiveLoad;
  /** Number of messages sent by the relay (per tick) */
  private int sendLoad;
  /** Number of messages sent by the relay while sending (per tick) */
  private int lostLoad;
  /**
   * Number of messages that are part of the retransmission protocol sent by the relay (per tick)
   */
  private int retransmissionLoad;
  /** Last generated waves (per tick) */
  @Nullable
  private Set<ProtocolWave> lastGeneratedWaves;

  /** Set of waves that have been received at this tick */
  private Set<ProtocolWave> lastDeliveredWaves;


  public Relay(int id, ContinuousSpace<Object> space, Network<Object> network) {
    this.id = id;
    this.space = space;
    this.network = network;
    this.setFrontier(new HashMap<Integer, Integer>());
    this.receivedWaves = new PriorityQueue<ProtocolWave>();
    this.outOfOrderWaves = new HashMap<Integer, PriorityQueue<ProtocolWave>>();
    this.style = RelayStyle.style.IDLE;
    this.neighboursLatency = new HashMap<Relay, Integer>();
    this.neighbours = new HashSet<Relay>();
    this.log = new HashMap<Integer, List<ProtocolWave>>();
    this.lastSentWaves = new LinkedList<Pair<Integer, ProtocolWave>>();
    this.lastGeneratedWaves = new HashSet<ProtocolWave>();
    this.lastDeliveredWaves = new HashSet<ProtocolWave>();
    schedule();
  }



  /**
   * Schedule all recurring actions based on run parameters. At every tick the Relay will process
   * received waves (onSense) and compute metrics. With different intervals it will also update its
   * neighbours, generate waves, and request retransmission to retrieve lost messages.
   */
  public void schedule() {
    int period = Configuration.PERIOD;
    int updateNeighboursRate = period * 10;
    int startTick = getCurrentTickAsInt() + 2;

    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
    schedule.schedule(createRepeating(startTick, 1, ScheduleParameters.FIRST_PRIORITY),
        this::first);
    schedule.schedule(createRepeating(startTick + updateNeighboursRate, updateNeighboursRate, 1),
        this::updateNeighbours);
    schedule.schedule(createRepeating(startTick, 1, 2), this::onSense);
    schedule.schedule(createRepeating(startTick, period, 3), this::generateWave);
    schedule.schedule(createRepeating(startTick + period, period, 4), this::requestRestransmission);
    schedule.schedule(createRepeating(startTick, period, 5), this::purgeLastSentWaves);
  }

  /** Called at every tick to reset metrics and representation fields */
  public void first() {
    this.style = RelayStyle.style.IDLE;
    this.receiveLoad = 0;
    this.sendLoad = 0;
    this.lostLoad = 0;
    this.retransmissionLoad = 0;
    this.lastGeneratedWaves.clear();
    this.lastDeliveredWaves.clear();
  }

  /**
   * When a relay joins the network, it updates its neighbours and broadcasts to its neighbours its
   * presence (through the addNeighbour method). It will compute an updated frontier based on the
   * nieghbours' information.
   */
  public void join() {
    this.updateNeighbours();
    for (Relay neighbour : this.getNeighbours()) {
      neighbour.addNeighbour(this);
    }
    requestRestransmission();
  }

  /**
   * Add a joining relay to the neighbours list, compute the latency based on its distance, and
   * return him the frontier
   * 
   * @param neighbour
   */
  private void addNeighbour(Relay neighbour) {

    this.neighbours.add(neighbour);
    this.computeNeighbourLatency(neighbour);
  }


  /** Discard all waves sent sooner than one period ago and log the information. */
  private void purgeLastSentWaves() {
    int lastPeriodStart = getCurrentTickAsInt() - Configuration.PERIOD;

    while (!this.lastSentWaves.isEmpty()
        && this.lastSentWaves.element().getLeft() < lastPeriodStart) {
      appendLog(this.lastSentWaves.remove().getRight());
    }
  }


  /** Find all relays within WAVE_PROPAGATION_DISTANCE in the continuous space */
  public void updateNeighbours() {
    this.neighboursLatency.clear();

    ContinuousWithin<Object> query =
        new ContinuousWithin<Object>(this.space, this, Configuration.WAVE_PROPAGATION_DISTANCE);

    this.setNeighbours(StreamSupport.stream(query.query().spliterator(), false).map(o -> (Relay) o)
        .peek(this::computeNeighbourLatency).collect(Collectors.toSet()));
  }

  /**
   * Compute a distance-based latency for a given neighbour
   * 
   * @param neighbour
   * @return the neighbour
   */
  private Relay computeNeighbourLatency(Relay neighbour) {
    double distance =
        this.space.getDistance(this.space.getLocation(this), this.space.getLocation(neighbour));
    int latency = (int) Math
        .floor((double) Configuration.PERIOD * distance / Configuration.WAVE_PROPAGATION_DISTANCE);
    this.neighboursLatency.put(neighbour, latency);
    return neighbour;
  }



  /**
   * Process all received-yet-not-delivered messages up to the currentTick. Note that we call the
   * receive message on the target node synchronously wrt. the sending of the message and we delay
   * the delivery. If relay A sends a message m to relay B at t, B will receive m at t, but it will
   * process m at t'>t.
   */
  public void onSense() {

    while (sendLoad < Configuration.MESSAGES_PER_TICK && !receivedWaves.isEmpty()
        && receivedWaves.element().getDeliveryTime() <= getCurrentTickAsInt()) {

      this.receiveLoad++;
      ProtocolWave wave = receivedWaves.remove();
      updateEdge(wave);

      if (wave instanceof RetransmissionProtocolWave) {
        this.retransmissionLoad++;
        forward(wave);
      } else {
        protocolForward(wave);
      }
    }
  }

  private void addWaveOutOfOrder(ProtocolWave wave) {
    if (!this.outOfOrderWaves.containsKey(wave.getSourceID())) {
      this.outOfOrderWaves.put(wave.getSourceID(),
          new PriorityQueue<ProtocolWave>(Comparator.comparingInt(ProtocolWave::getReference)));
    }
    this.outOfOrderWaves.get(wave.getSourceID()).add(wave);
  }

  /**
   * 
   * @param wave
   */
  public void receive(Wave wave) {
    if (!(wave instanceof RetransmissionProtocolWave)
        && RandomHelper.nextDouble() <= Configuration.MESSAGE_LOSS_PROBABILITY) {
      this.lostLoad++;
      if (wave instanceof ProtocolWave) {
        this.updateEdge((ProtocolWave) wave);
      }
    } else {
      if (wave instanceof RetransmissionRequestWave) {

        // We need to check also for keys that have never been received by the relay asking for the
        // retransmission
        Set<Integer> keys =
            new HashSet<Integer>(((RetransmissionRequestWave) wave).getFrontier().keySet());
        keys.addAll(this.getFrontier().keySet());

        for (Integer sourceID : keys) {
          if (this.log.containsKey(sourceID)) {
            this.log.get(sourceID).stream()
                .filter(protocolWave -> !((RetransmissionRequestWave) wave).getFrontier()
                    .containsKey(sourceID)
                    || protocolWave.getReference() >= ((RetransmissionRequestWave) wave)
                        .getFrontier().get(sourceID))
                .forEach(protocolWave -> receivedWaves
                    .add(new RetransmissionProtocolWave(sourceID, protocolWave.getReference(),
                        protocolWave.getValue(), wave.getDeliveryTime(), wave.getForwarderID())));
          }
        }
      } else if (wave instanceof ProtocolWave) {
        receivedWaves.add((ProtocolWave) wave);
      }
    }
  }

  /**
   * 
   */
  public void generateWave() {
    boolean shouldGenerate =
        (getCurrentTickAsInt() / Configuration.PERIOD) < Configuration.WAVE_GENERATION_LAST_PERIOD;

    if (shouldGenerate) {
      for (int i = 0; i < Configuration.GenerateN(); i++) {
        ProtocolWave wave =
            new ProtocolWave(this.id, this.currentReference, getCurrentTickAsInt(), this.id);
        this.currentReference = nextReference(this.currentReference);
        this.getFrontier().put(this.id, currentReference);
        forward(wave);
        setNodeStyle(this.id, RelayStyle.style.SEND);
        this.lastGeneratedWaves.add(new ProtocolWave(wave));
      }
    }
  }


  /**
   * 
   * @param wave
   */
  private void forward(ProtocolWave wave) {
    this.lastSentWaves.add(Pair.of(getCurrentTickAsInt(), new ProtocolWave(wave)));
    this.sendLoad++;

    for (Relay neighbour : this.neighbours) {
      if (getContext(neighbour) != null) {

        ProtocolWave waveForNeighbour = new ProtocolWave(wave);
        waveForNeighbour.setDeliveryTime(computeDeliveryTime(neighbour));
        waveForNeighbour.setForwarderID(this.id);

        setEdgeStyle(wave.getSourceID(), neighbour);
        setNodeStyle(wave.getSourceID(), RelayStyle.style.FORWARD);

        neighbour.receive(waveForNeighbour);
      }
    }
  }


  /**
   * Waves that are part of the protocol should be handled differently from retransmission waves.
   * When a relay delivers a wave, it checks whether it has to process it and whether the wave is
   * newer than the once it has already received; if the reference of the wave is the one expected
   * it will process and forward it to its neighbours, otherwise it will add it to the
   * outOfOrderWaves for future processing. When a wave is successfully processed,
   * already-received-yet-not-delivered waves with same SourceID and higher reference
   * (outOfOrderWaves) are also processed.
   */
  private void protocolForward(ProtocolWave wave) {

    if (wave.getSourceID() != this.id) {

      int expectedNextReference = 0;
      if (this.getFrontier().containsKey(wave.getSourceID())) {
        expectedNextReference = this.getFrontier().get(wave.getSourceID());
      }

      if (expectedNextReference == wave.getReference()) {
        forward(wave);
        this.lastDeliveredWaves.add(new ProtocolWave(wave));
        expectedNextReference = nextReference(wave.getReference());

        if (this.outOfOrderWaves.containsKey(wave.getSourceID())) {
          PriorityQueue<ProtocolWave> queue = this.outOfOrderWaves.get(wave.getSourceID());


          while (!queue.isEmpty() && queue.element().getReference() <= expectedNextReference) {
            ProtocolWave outOfOrderWave = queue.element();

            if (outOfOrderWave.getReference() < expectedNextReference) {
              queue.remove();
            }

            if (outOfOrderWave.getReference() == expectedNextReference
                && sendLoad < Configuration.MESSAGES_PER_TICK) {
              queue.remove();
              forward(outOfOrderWave);
              this.lastDeliveredWaves.add(new ProtocolWave(outOfOrderWave));
              expectedNextReference = nextReference(expectedNextReference);
            }
          }
        }

        this.getFrontier().put(wave.getSourceID(), expectedNextReference);
      } else if (expectedNextReference < wave.getReference()) {
        addWaveOutOfOrder(new ProtocolWave(wave));
      }
    }
  }


  /**
   * Modify the weight of the network edges based on the number of messages that are being
   * transmitted on the given edge. Edges are directional and get removed if no message is being
   * exchanged onto them.
   */
  private void updateEdge(ProtocolWave wave) {
    Optional<Relay> sender =
        StreamSupport.stream(this.network.getAdjacent(this).spliterator(), false)
            .map(o -> (Relay) o).filter(n -> n.getId() == wave.getForwarderID()).findFirst();
    if (sender.isPresent()) {
      RepastEdge<Object> edge = this.network.getEdge(sender.get(), this);
      if (edge != null) {
        double weight = edge.getWeight();
        if (weight > 1) {
          edge.setWeight(weight - 1);
        } else {
          this.network.removeEdge(edge);
        }
      }
    }
  }


  private int computeDeliveryTime(Relay neighbour) {
    return getCurrentTickAsInt() + neighboursLatency.get(neighbour)
        + RandomHelper.nextIntFromTo(0, Configuration.MAX_LATENCY);
  }


  public void requestRestransmission() {
    for (Relay neighbour : this.neighbours) {
      neighbour.receive(new RetransmissionRequestWave(this.id, getCurrentTickAsInt(), this.frontier,
          computeDeliveryTime(neighbour)));
    }
  }


  private Integer nextReference(int reference) {
    return reference + 1;
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setNeighbours(Set<Relay> neighbours) {
    this.neighbours = neighbours;
  }

  public Set<Relay> getNeighbours() {
    return neighbours;
  }

  public int getNeighboursCount() {
    return neighbours.size();
  }

  private double getCurrentTick() {
    return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
  }

  private int getCurrentTickAsInt() {
    return (int) getCurrentTick();
  }

  public RelayStyle.style getStyle() {
    return style;
  }

  public void setStyle(RelayStyle.style style) {
    this.style = style;
  }

  public int getReceiveLoad() {
    return receiveLoad;
  }

  public int getSendLoad() {
    return sendLoad;
  }

  public Set<ProtocolWave> getLastGeneratedWave() {
    return lastGeneratedWaves;
  }

  public Set<ProtocolWave> getLastReceivedWaves() {
    return lastDeliveredWaves;
  }

  public void setLatency(Relay neighbour, int latency) {
    neighboursLatency.put(neighbour, latency);
  }

  public int getLostLoad() {
    return this.lostLoad;
  }

  public int getRetransmissionLoad() {
    return this.retransmissionLoad;
  }

  public void clearNeighboursLatency() {
    this.neighboursLatency.clear();
  }

  /**
   * The node style is set depending on the sourceID. We exploit this parameter to isolate parallel
   * messages.
   * 
   * @param sourceID wave source ID
   * @param style
   */
  private void setNodeStyle(int sourceID, RelayStyle.style style) {
    if (sourceID == 0) {
      this.style = style;
    }
  }

  private void setEdgeStyle(int sourceID, Relay neighbour) {
    CustomEdge<Object> edge = (CustomEdge<Object>) this.network.getEdge(this, neighbour);
    if (edge == null) {
      edge = (CustomEdge<Object>) this.network.addEdge(this, neighbour, 1);
    } else {
      edge.setWeight(edge.getWeight() + 1);
    }

    if (sourceID == 0) {
      edge.setShowInNetwork(true);
    }
  }

  public Map<Integer, Integer> getFrontier() {
    return frontier;
  }

  public void setFrontier(Map<Integer, Integer> frontier) {
    this.frontier = frontier;
  }

  private void appendLog(ProtocolWave wave) {
    if (!this.log.containsKey(wave.getSourceID())) {
      this.log.put(wave.getSourceID(), new ArrayList<>());
    }
    this.log.get(wave.getSourceID()).add(new ProtocolWave(wave));
  }

  public Map<Integer, List<ProtocolWave>> getLog() {
    return log;
  }

  public int getOutOfOrderWavesSize() {
    int sum = 0;
    for (PriorityQueue<ProtocolWave> queue : this.outOfOrderWaves.values()) {
      sum += queue.size();
    }
    return sum;
  }
}
