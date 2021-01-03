package wavecast.configuration;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

/**
 * A wrapper for all configuration parameters that allows to retrieve them once at the beginning of
 * the simulation and to modify them at runtime
 */
public class WavecastConfiguration {

  public static int SEED;
  public static int SPACE_HEIGHT;
  public static int SPACE_WIDTH;

  /**
   * Specifies the ratio ticks over simulation PERIOD. Major operations (node joins,
   * retransmissions) take place at multiples of the PERIOD, whereas the protocol exchanges messages
   * at fractions of the PERIOD, whose number is given by the TICK_ACCURACY.
   */
  public static double TICK_ACCURACY;

  /** A number of ticks */
  public static int PERIOD;

  /** Number of relays at the beginning of the simulation */
  public static int RELAYS_NUMBER;

  /** Maximum number of relays that can join at every period */
  public static int RELAY_JOIN_MAX_NUMBER;

  /** Probability that a number [0,RELAY_JOIN_MAX_NUMBER] of relays join at every period */
  public static double RELAY_JOIN_PROBABILITY;

  /** Period after which no relay can join the simulation */
  public static double RELAY_JOIN_MAX_PERIOD;

  /**
   * Maximum distance of waves propagation: it determines the neighbours a relay has; the following
   * condition applies: a wave propagates at speed WAVE_PROPAGATION_DISTANCE/PERIOD
   */
  public static int WAVE_PROPAGATION_DISTANCE;

  /**
   * Maximum number of messages a Relay can send per tick. This is equivalent to the link bandwidth
   * in usual networks
   */
  public static int MESSAGES_PER_TICK;

  /** Probability that a message is lost during transmission */
  public static double MESSAGE_LOSS_PROBABILITY;

  /**
   * Value in [0,1] that defines how much random latency to introduce in message exchanges as a
   * fraction of the simulation PERIOD
   */
  public static double LATENCY_RATIO;

  /** Maximum delay (in ticks) introduced in a message exchange (derived from LATENCY_RATIO) */
  public static int MAX_LATENCY;

  /**
   * Parameter used to configure the appearance of loads. LOAD_COLOR_SCALE=10 is equivalent to say
   * that load colors should change at multiples of 10
   */
  public static int LOAD_COLOR_SCALE;

  /** Load the parameters from the Repast simulation */
  public static void load() {
    Parameters p = (Parameters) RunEnvironment.getInstance().getParameters();
    WavecastConfiguration.SEED = p.getInteger("randomSeed");
    WavecastConfiguration.RELAYS_NUMBER = p.getInteger("relaysNumber");
    WavecastConfiguration.SPACE_HEIGHT = p.getInteger("spaceHeight");
    WavecastConfiguration.SPACE_WIDTH = p.getInteger("spaceWidth");
    WavecastConfiguration.WAVE_PROPAGATION_DISTANCE = p.getInteger("wavePropagationDistance");
    WavecastConfiguration.MESSAGES_PER_TICK = p.getInteger("messagesPerTick");
    WavecastConfiguration.TICK_ACCURACY = p.getDouble("tickAccuracy");
    WavecastConfiguration.PERIOD = (int) (1. / WavecastConfiguration.TICK_ACCURACY);
    WavecastConfiguration.LOAD_COLOR_SCALE = p.getInteger("loadColorScale");
    WavecastConfiguration.RELAY_JOIN_MAX_PERIOD = p.getInteger("relayJoinMaxPeriod");
    WavecastConfiguration.RELAY_JOIN_MAX_NUMBER = p.getInteger("relayJoinMaxNumber");
    WavecastConfiguration.RELAY_JOIN_PROBABILITY = p.getDouble("relayJoinProbability");
    WavecastConfiguration.LATENCY_RATIO = p.getDouble("latencyRatio");
    WavecastConfiguration.MAX_LATENCY = (int) (PERIOD * LATENCY_RATIO);
    WavecastConfiguration.MESSAGE_LOSS_PROBABILITY = p.getDouble("lossProbability");

    RandomHelper.setSeed(WavecastConfiguration.SEED);
  }
}
