package wavecast.context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import cern.jet.random.Uniform;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.InfiniteBorders;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;
import scuttlebutt.store.Store;
import wavecast.configuration.WavecastConfiguration;
import wavecast.network.CustomEdgeCreator;
import wavecast.relay.Relay;

public class Builder implements ContextBuilder<Object> {

  private ContinuousSpace<Object> continuousSpace;
  private Network<Object> network;
  private Context<Object> context;
  private Context<Object> scuttlebuttContext;

  /** Used to know what id to assign to joining nodes */
  private int nextRelayID;

  @Override
  public Context<Object> build(Context<Object> scuttlebuttContext) {
    this.scuttlebuttContext = scuttlebuttContext;
    this.context = (Context<Object>) scuttlebuttContext.getSubContext("wavecast");

    WavecastConfiguration.load();

    // Initialize continuous space
    ContinuousSpaceFactory continuousSpaceFactory =
        ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
    this.continuousSpace = continuousSpaceFactory.createContinuousSpace("space", context,
        new RandomCartesianAdder<Object>(), new InfiniteBorders<Object>(),
        WavecastConfiguration.SPACE_WIDTH, WavecastConfiguration.SPACE_HEIGHT);

    // Initialize the display network
    NetworkBuilder<Object> builder = new NetworkBuilder<Object>("network", context, true)
        .setEdgeCreator(new CustomEdgeCreator<Relay>());
    this.network = builder.buildNetwork();

    // Initialize nodes
    for (int i = 0; i < WavecastConfiguration.RELAYS_NUMBER; i++) {
      context.add(new Relay(i, continuousSpace, this.network));
    }
    nextRelayID = WavecastConfiguration.RELAYS_NUMBER;

    // Enforce all relays have at least one neighbour
    checkReachable(context, continuousSpace);

    // Schedule relays to join during simulation
    RunEnvironment.getInstance().getCurrentSchedule()
        .schedule(ScheduleParameters.createRepeating(2 + WavecastConfiguration.PERIOD,
            WavecastConfiguration.PERIOD, ScheduleParameters.FIRST_PRIORITY), this::joinRelays);

    return context;
  }

  /**
   * Enforce all relays have at least one neighbour, i.e. check whether the
   * WAVE_PROPAGATION_DISTANCE is sufficiently big, otherwise increase it as necessary.
   */
  private void checkReachable(Context<Object> context, ContinuousSpace<Object> continuousSpace) {

    Set<Relay> reachableRelays;
    boolean bigEnough = false;

    while (!bigEnough) {

      context.getObjectsAsStream(Relay.class).map(o -> (Relay) o).forEach(relay -> {
        relay.updateNeighbours();
      });


      // BFS on relays
      Queue<Relay> queue = new LinkedList<>();
      Relay root = (Relay) context.getRandomObjectsAsStream(Relay.class, 1).findFirst().get();
      reachableRelays = new HashSet<Relay>(root.getNeighbours());

      for (Relay relay : reachableRelays) {
        queue.add(relay);
      }

      Relay tmp;
      while (!queue.isEmpty()) {
        tmp = queue.remove();
        for (Relay neighbour : tmp.getNeighbours()) {
          if (!reachableRelays.contains(neighbour)) {
            queue.add(neighbour);
            reachableRelays.add(neighbour);
          }
        }
      }

      if (reachableRelays.size() == WavecastConfiguration.RELAYS_NUMBER) {
        bigEnough = true;
      } else {
        WavecastConfiguration.WAVE_PROPAGATION_DISTANCE++;
      }
    }
  }


  /**
   * Configuration parameters can be used to specify how often, until when and how many relays can
   * join during the simulation.
   */
  private void joinRelays() {

    Uniform relayJoinUniform = RandomHelper.createUniform(0, 1);
    Uniform relayJoinMaxNumberUniform = RandomHelper.createUniform(0, 1);

    int currentTick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
    boolean shouldJoin =
        currentTick / WavecastConfiguration.PERIOD <= WavecastConfiguration.RELAY_JOIN_MAX_PERIOD
            && relayJoinUniform.nextDouble() <= WavecastConfiguration.RELAY_JOIN_PROBABILITY;

    if (shouldJoin) {
      int joining =
          relayJoinMaxNumberUniform.nextIntFromTo(0, WavecastConfiguration.RELAY_JOIN_MAX_NUMBER);
      ArrayList<Relay> relays = new ArrayList<Relay>(joining);
      while (joining > 0) {
        Relay relay = new Relay(this.nextRelayID++, continuousSpace, this.network);
        relays.add(relay);
        context.add(relay);
        joining--;
      }

      // Defer neighbours computation after all nodes have been added to the context
      for (Relay relay : relays) {
        relay.join();
        createStore(relay);
      }
    }
  }

  public void createStore(Relay relay) {
    Store store = new Store(relay);
    this.scuttlebuttContext.add(store);

    ContinuousSpace<Object> storesSpace =
        (ContinuousSpace<Object>) scuttlebuttContext.getProjection("storeSpace");
    storesSpace.moveTo(store, relay.getLocation().getX(), relay.getLocation().getY());

    Network<Object> storesNetwork =
        (Network<Object>) scuttlebuttContext.getProjection("storeNetwork");

    for (Relay neighbour : relay.getNeighbours()) {
      Store neighbourStore = neighbour.getStore();
      if (scuttlebuttContext.contains(neighbourStore)) {
        storesNetwork.addEdge(store, neighbourStore);
        storesNetwork.addEdge(neighbourStore, store);
      }
    }

    relay.setStore(store);
  }
}
