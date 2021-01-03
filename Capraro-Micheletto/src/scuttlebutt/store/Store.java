package scuttlebutt.store;

import static repast.simphony.engine.schedule.ScheduleParameters.createRepeating;
import static repast.simphony.util.ContextUtils.getContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import cern.jet.random.Uniform;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.random.RandomHelper;
import scuttlebutt.configuration.ScuttlebuttConfiguration;
import scuttlebutt.crypto.Crypto;
import scuttlebutt.crypto.EncryptedPayload;
import scuttlebutt.event.Content;
import scuttlebutt.event.Event;
import scuttlebutt.exception.CryptoException;
import scuttlebutt.exception.SerializationException;
import scuttlebutt.log.Log;
import scuttlebutt.payload.Payload;
import scuttlebutt.serialization.Serializer;
import scuttlebutt.store.protocol.OpenGossipProtocol;
import scuttlebutt.store.protocol.Protocol;
import scuttlebutt.store.protocol.TransitiveInterestProtocol;
import scuttlebutt.styles.StoreReplicationStyle;
import wavecast.configuration.WavecastConfiguration;
import wavecast.relay.Relay;

/**
 * This class represents the Store described in the Scuttlebutt paper. Each store is associated to a
 * Wavecast relay
 */
public class Store {

  private static final Logger LOGGER = Logger.getLogger(Store.class.getName());

  /** Map of all the logs stored by the store */
  private Map<PublicKey, Log> logs;

  /** Reference of the relay on which the store is located */
  private Relay relay;

  /** Instance of the crypto class, to provide crypto functionalities */
  private Crypto crypto;

  /** Private key of the store */
  private PrivateKey privateKey;

  /** Public key of the store */
  private PublicKey publicKey;

  /**
   * Default uniform distribution in the range [0,1]. Note: the default uniform can be used by
   * multiple sources as a random number generator whenever the uniformity requirement is not
   * needed; for other uses independent distributions should be generated.
   */
  private Uniform defaultUniform;

  /** Uniform distribution in the range [0,1], used for the event generation */
  private Uniform eventGenerationUniform;

  /**
   * Uniform distribution in the range [0,heartbeatPeriodTicks], used for the heartbeat generation
   */
  private Uniform heartbeatUniform;

  /** Uniform distribution in the range [0,period], used for the update triggering */
  private Uniform updateUniform;

  /**
   * Uniform distribution in the range [0,1], used when choosing whether to follow on heartbeat
   * reception
   */
  public Uniform followProbabilityUniform;

  /**
   * Uniform distribution in the range [0,1], used when choosing whether to unfollow on heartbeat
   * reception
   */
  public Uniform unfollowProbabilityUniform;

  /**
   * Uniform distribution in the range [0,1], used when choosing whether to block on heartbeat
   * reception
   */
  public Uniform blockProbabilityUniform;

  /**
   * Uniform distribution in the range [0,1], used when choosing whether to unblock on heartbeat
   * reception
   */
  public Uniform unblockProbabilityUniform;

  /** ISchedulableAction used to uniformly schedule event generation over time */
  private ISchedulableAction generateEventSchedule;

  /** ISchedulableAction used to uniformly schedule heartbeat generation over time */
  private ISchedulableAction generateHeartbeatSchedule;

  /** Number of news received in last tick used to plot statistics */
  private int newsReceivedInLastTick;

  /** Chosen protocol (OpenGossip/TransitiveInterest) */
  private Protocol protocol;

  /** Set of directed followee used to compute statistics */
  private Set<PublicKey> directFollowee;

  /** Set of transitive followee used to compute statistics */
  private Set<PublicKey> transitiveFollowee;

  /** Set of blocked stores used to compute statistics */
  private Set<PublicKey> blocked;

  /** Style of the store for the visualization on a display */
  private StoreReplicationStyle.style style;


  /**
   * The constructor initialize the variables, in particular the cryptographic keys, insert itself
   * in the logs and select the chosen protocol
   * 
   * @param relay: the relay associated with the stores
   */
  public Store(Relay relay) {
    this.logs = new HashMap<PublicKey, Log>();
    this.relay = relay;
    this.crypto = new Crypto();
    KeyPair keyPair = crypto.newKeyPair();
    this.privateKey = keyPair.getPrivate();
    this.publicKey = keyPair.getPublic();
    this.style = StoreReplicationStyle.style.NONE;

    // Add the store self log
    this.logs.put(this.publicKey, new Log(this.publicKey));

    if (ScuttlebuttConfiguration.PROTOCOL.equals(ScuttlebuttConfiguration.OPEN_GOSSIP_PROTOCOL)) {
      protocol = new OpenGossipProtocol();
    } else {
      protocol = new TransitiveInterestProtocol();
      // Follow myself
      ((TransitiveInterestProtocol) protocol).follow(this, this.publicKey);
    }

    schedule();
  }


  /**
   * Schedule all the repeating actions
   */
  public void schedule() {
    int period = WavecastConfiguration.PERIOD;
    int heartbeatPeriodTicks = ScuttlebuttConfiguration.HEARTBEAT_RATE_PERIODS * period;
    int startTick = Relay.getCurrentTickAsInt() + 3;

    this.defaultUniform = RandomHelper.createUniform(0, 1);
    this.eventGenerationUniform = RandomHelper.createUniform(0, 1);
    this.heartbeatUniform = RandomHelper.createUniform(0, heartbeatPeriodTicks);
    this.updateUniform = RandomHelper.createUniform(0, period);
    this.followProbabilityUniform = RandomHelper.createUniform(0, 1);
    this.unfollowProbabilityUniform = RandomHelper.createUniform(0, 1);
    this.blockProbabilityUniform = RandomHelper.createUniform(0, 1);
    this.unblockProbabilityUniform = RandomHelper.createUniform(0, 1);

    ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
    schedule.schedule(createRepeating(startTick, 1, 5), this::first);
    schedule.schedule(createRepeating(startTick, 1, 6), this::setReplicationStyle);

    generateHeartbeatSchedule = schedule
        .schedule(createRepeating(startTick, heartbeatPeriodTicks, 7), this::scheduleHeartbeat);
    schedule.schedule(createRepeating(startTick, period, 8), this::scheduleUpdates);
    generateEventSchedule =
        schedule.schedule(createRepeating(startTick, 1, 11), this::generateEvent);
  }


  /**
   * Called at every tick to reset metrics
   */
  public void first() {
    this.newsReceivedInLastTick = 0;
  }


  /**
   * Set the proper style to be represented to the user in the followee display
   */
  public void setReplicationStyle() {
    String storeToHighlight = "scuttlebutt.store."
        + RunEnvironment.getInstance().getParameters().getString("storeToHighlight");

    if (this.toString().equals(storeToHighlight)) {
      this.setStyle(StoreReplicationStyle.style.MAIN);

      for (Object obj : getContext(this)) {
        if (obj instanceof Store) {
          Store otherStore = (Store) obj;
          if (!otherStore.toString().equals(storeToHighlight)) {
            if (directFollowee.contains(otherStore.getPublicKey())) {
              otherStore.setStyle(StoreReplicationStyle.style.DIRECTED);
            } else if (transitiveFollowee.contains(otherStore.getPublicKey())) {
              otherStore.setStyle(StoreReplicationStyle.style.TRANSITIVE);
            } else if (blocked.contains(otherStore.getPublicKey())) {
              otherStore.setStyle(StoreReplicationStyle.style.BLOCKED);
            } else {
              otherStore.setStyle(StoreReplicationStyle.style.NONE);
            }
          }
        }
      }
    }
  }


  /**
   * Uniformly schedule heartbeat generation over time
   */
  private void scheduleHeartbeat() {
    double currentTick = Relay.getCurrentTick();
    RunEnvironment.getInstance().getCurrentSchedule().schedule(
        ScheduleParameters.createOneTime(currentTick + heartbeatUniform.nextInt(), 9),
        this::broadcastHeartbeat);
  }


  /**
   * Uniformly schedule update triggering over time
   */
  private void scheduleUpdates() {
    double currentTick = Relay.getCurrentTick();
    RunEnvironment.getInstance().getCurrentSchedule().schedule(
        ScheduleParameters.createOneTime(currentTick + updateUniform.nextInt(), 10),
        this::triggerUpdate);
  }


  /**
   * Trigger the update process by sending a PULL payload
   */
  public void triggerUpdate() {
    int numberOfUpdates = ScuttlebuttConfiguration.UPDATES_PER_PERIOD;
    getContext(this).getRandomObjectsAsStream(Store.class, numberOfUpdates + 1)
        .filter(store -> !((Store) store).getPublicKey().equals(this.getPublicKey()))
        .limit(numberOfUpdates).forEach(obj -> {
          Store other = (Store) obj;
          Payload payload = new Payload(this.publicKey, protocol.getFrontier(this));
          String encryptedPayload = "";
          try {
            encryptedPayload = crypto.encrypt(other.getPublicKey(), Serializer.serialize(payload));
          } catch (CryptoException | SerializationException e) {
            LOGGER.log(Level.SEVERE, "Failed to encrypt payload ", e);
          }
          relay.sendWave(encryptedPayload);
        });

  }


  /**
   * Broadcast heartbeat with my id (in clear), to let stores know that I exist
   */
  public void broadcastHeartbeat() {
    Payload payload = new Payload(this.publicKey);
    try {
      relay.sendWave(Serializer.serialize(
          new EncryptedPayload(Serializer.serialize(payload).getBytes(StandardCharsets.UTF_8))));
    } catch (SerializationException e) {
      LOGGER.log(Level.SEVERE, "Failed to broadcast heartbeat", e);
    }
  }


  /**
   * Generate new event and store it in my logs
   */
  public void generateEvent() {
    if (Relay.getCurrentTickAsInt() > ScuttlebuttConfiguration.EVENTS_GENERATION_MAX_PERIOD
        * WavecastConfiguration.PERIOD) {
      RunEnvironment.getInstance().getCurrentSchedule().removeAction(generateEventSchedule);
      RunEnvironment.getInstance().getCurrentSchedule().removeAction(generateHeartbeatSchedule);
    } else if (eventGenerationUniform
        .nextDouble() <= ScuttlebuttConfiguration.EVENT_GENERATION_PROBABILITY) {
      try {
        Content content = new Content(String.valueOf(defaultUniform.nextInt()));
        logs.get(this.publicKey).append(content, privateKey, crypto);
      } catch (SerializationException e) {
        LOGGER.log(Level.SEVERE, "Failed to generate event", e);
      }
    }
  }


  /**
   * Handle message received by underlying relay
   * 
   * @param encryptedPayload: payload delivered from the network
   */
  public void deliver(String encryptedPayload) {
    try {
      String decryptedPayload = crypto.decrypt(privateKey, encryptedPayload);
      Payload payload = (new Serializer<Payload>()).deserialize(decryptedPayload);
      switch (payload.getType()) {
        case PULL: {
          List<Event> news = this.since(payload.getFrontier());
          Payload responsePayload = new Payload(this.publicKey, protocol.getFrontier(this), news);
          String encryptedResponsePayload =
              crypto.encrypt(payload.getSenderId(), Serializer.serialize(responsePayload));
          relay.sendWave(encryptedResponsePayload);
          break;
        }
        case PUSHPULL: {
          List<Event> news = this.since(payload.getFrontier());
          Payload responsePayload = new Payload(this.publicKey, news);
          String encryptedResponsePayload =
              crypto.encrypt(payload.getSenderId(), Serializer.serialize(responsePayload));
          this.update(payload.getNews());
          relay.sendWave(encryptedResponsePayload);
          break;
        }
        case PUSH: {
          this.update(payload.getNews());
          break;
        }
        case HEARTBEAT: {
          PublicKey id = payload.getSenderId();
          if (!logs.containsKey(id)) {
            protocol.processHeartbeat(this, id);
          }
          break;
        }
        default:
          LOGGER.log(Level.INFO, "Unrecognized type for store payload");
      }
    } catch (CryptoException | SerializationException e) {
      // The message was not for me
    }
  }


  /**
   * Get the list of events that happened after frontier
   * 
   * @param frontier on which to compute the since operation
   * @return the list of events that happened after the frontier
   */
  public List<Event> since(Frontier frontier) {
    List<Event> news = new ArrayList<Event>();
    Frontier frontierThis = protocol.getFrontier(this);

    for (PublicKey id : frontier.keySet()) {
      if (frontierThis.containsKey(id)) {
        Log log = this.logs.get(id);
        for (Event event : log.get()) {
          if (event.getIndex() > frontier.get(id)) {
            news.add(new Event(event));
          }
        }
      }
    }
    return news;
  }


  /**
   * Update the logs in store with new events
   * 
   * @param news to be inserted in the logs
   */
  public void update(List<Event> news) {
    Frontier frontier = protocol.getFrontier(this);

    for (Event event : news) {
      PublicKey id = event.getId();

      try {
        String signature = crypto.decrypt(id, event.getSignature());

        Event eventWithoutSignature =
            new Event(id, event.getPrevious(), event.getIndex(), event.getContent());
        String expectedSignature = Serializer.serialize(eventWithoutSignature);

        // check signature
        if (expectedSignature.equals(signature)) {
          if (frontier.containsKey(id)) {
            Log log = logs.get(id);
            // Check that the update has not been received from another store yet
            if (log.getLastEventStored() < event.getIndex()) {
              this.newsReceivedInLastTick++;
              // FIXME: we should change the news as a map<PublicKey,List<Event> or at least build
              // the
              // map instead of using a one-item temporary list
              List<Event> tempList = new ArrayList<Event>();
              tempList.add(event);
              logs.get(id).update(tempList);
            }
          }
        }
      } catch (CryptoException | SerializationException e) {
        LOGGER.log(Level.SEVERE, "Failed to check signature", e);
      }
    }
  }


  /**
   * Add a log to the store
   * 
   * @param log to be added
   * @return store with the added log
   */
  public Store add(Log log) {
    PublicKey id = log.getId();
    if (!logs.containsKey(id)) {
      logs.put(id, new Log(log));
    }
    return this;
  }


  /**
   * Append a new log locally created
   * 
   * @param content: the content of the new log to be created
   */
  public void append(Content content) {
    try {
      this.get(this.publicKey).append(content, privateKey, crypto);
    } catch (SerializationException e) {
      LOGGER.log(Level.SEVERE, "Failed to append content to log", e);
    }
  }


  /**
   * Remove the log with the specified id from the store
   * 
   * @param id of the log to be removed
   * @return the store without the removed log
   */
  public Store remove(PublicKey id) {
    logs.remove(id);
    return this;
  }


  /**
   * Get the log with id from the store (if present)
   * 
   * @param id of the log to be returned
   * @return the log with the specified id if present, null otherwise
   */
  public Log get(PublicKey id) {
    return logs.get(id);
  }


  /**
   * Get the set of ids of the logs in the store
   * 
   * @return the set of ids
   */
  public Set<PublicKey> ids() {
    return logs.keySet();
  }


  public PublicKey getPublicKey() {
    return this.publicKey;
  }


  public Map<PublicKey, Log> getLogs() {
    return this.logs;
  }


  /**
   * Return the total number of events stored in all the logs of the store, for statistics
   * 
   * @return the number of events stored
   */
  public int getNumberOfEvents() {
    int sum = 0;
    for (Log log : getLogs().values()) {
      sum += log.get().size();
    }
    return sum;
  }


  /**
   * Return the number of logs stored in the store, for statistics
   * 
   * @return the number of logs stored
   */
  public int getNumberOfLogs() {
    return getLogs().size();
  }


  public int getNewsReceivedInLastTick() {
    return this.newsReceivedInLastTick;
  }


  /**
   * Compute the frontier based on the chosen protocol
   * 
   * @return the computed frontier
   */
  public Frontier getFrontier() {
    return protocol.getFrontier(this);
  }


  public void setDirectFollowee(Set<PublicKey> directFollowee) {
    this.directFollowee = directFollowee;
  }


  public void setTransitiveFollowee(Set<PublicKey> transitiveFollowee) {
    this.transitiveFollowee = transitiveFollowee;
  }


  public void setBlocked(Set<PublicKey> blocked) {
    this.blocked = blocked;
  }


  public int getNumberOfDirectFollowee() {
    if (directFollowee == null) {
      return 0;
    }
    return directFollowee.size();
  }


  public int getNumberOfTransitiveFollowee() {
    if (transitiveFollowee == null) {
      return 0;
    }
    return transitiveFollowee.size();
  }


  public int getNumberOfBlocked() {
    if (blocked == null) {
      return 0;
    }
    return blocked.size();
  }


  public StoreReplicationStyle.style getStyle() {
    return style;
  }


  public void setStyle(StoreReplicationStyle.style style) {
    this.style = style;
  }

}
