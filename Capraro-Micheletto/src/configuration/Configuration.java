package configuration;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

/**
 * A wrapper for all configuration parameters that allows to retrieve them once at the beginning of
 * the simulation and to modify them at runtime
 */
public class Configuration {

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
   * Period after which no wave is generated anymore. Use it to test the correctness of the protocol
   */
  public static int WAVE_GENERATION_LAST_PERIOD;

  /** Probability distribution used by the relay to generate wave (at every period) */
  public static String WAVE_GENERATION_DISTRIBUTION;
  /** Mean of the distribution or left bound (for uniform) */
  public static double WAVE_GENERATION_DISTRIBUTION_PARAM1;
  /** Variance of the distribution if allowed or right bound (for uniform) */
  public static double WAVE_GENERATION_DISTRIBUTION_PARAM2;

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
    Configuration.SEED = p.getInteger("randomSeed");
    Configuration.RELAYS_NUMBER = p.getInteger("relaysNumber");
    Configuration.SPACE_HEIGHT = p.getInteger("spaceHeight");
    Configuration.SPACE_WIDTH = p.getInteger("spaceWidth");
    Configuration.WAVE_GENERATION_LAST_PERIOD = p.getInteger("waveGenerationLastPeriod");
    Configuration.WAVE_GENERATION_DISTRIBUTION = p.getString("waveGenerationDistribution");
    Configuration.WAVE_GENERATION_DISTRIBUTION_PARAM1 =
        p.getDouble("waveGenerationDistributionParam1");
    Configuration.WAVE_GENERATION_DISTRIBUTION_PARAM2 =
        p.getDouble("waveGenerationDistributionParam2");
    Configuration.WAVE_PROPAGATION_DISTANCE = p.getInteger("wavePropagationDistance");
    Configuration.MESSAGES_PER_TICK = p.getInteger("messagesPerTick");
    Configuration.TICK_ACCURACY = p.getDouble("tickAccuracy");
    Configuration.PERIOD = (int) (1. / Configuration.TICK_ACCURACY);
    Configuration.LOAD_COLOR_SCALE = p.getInteger("loadColorScale");
    Configuration.RELAY_JOIN_MAX_PERIOD = p.getInteger("relayJoinMaxPeriod");
    Configuration.RELAY_JOIN_MAX_NUMBER = p.getInteger("relayJoinMaxNumber");
    Configuration.RELAY_JOIN_PROBABILITY = p.getDouble("relayJoinProbability");
    Configuration.LATENCY_RATIO = p.getDouble("latencyRatio");
    Configuration.MAX_LATENCY = (int) (PERIOD * LATENCY_RATIO);
    Configuration.MESSAGE_LOSS_PROBABILITY = p.getDouble("lossProbability");


    RandomHelper.setSeed(Configuration.SEED);
    switch (WAVE_GENERATION_DISTRIBUTION) {
      case "NORMAL":
        RandomHelper.createNormal(WAVE_GENERATION_DISTRIBUTION_PARAM1,
            WAVE_GENERATION_DISTRIBUTION_PARAM2);
        break;
      case "EXPONENTIAL":
        RandomHelper.createExponential(1 / WAVE_GENERATION_DISTRIBUTION_PARAM1);
        break;
      case "UNIFORM":
      default:
        RandomHelper.createUniform(WAVE_GENERATION_DISTRIBUTION_PARAM1,
            WAVE_GENERATION_DISTRIBUTION_PARAM2);
    }
  }


  public static int GenerateN() {
    switch (WAVE_GENERATION_DISTRIBUTION) {
      case "NORMAL":
        return RandomHelper.getNormal().nextInt();
      case "EXPONENTIAL":
        return RandomHelper.getExponential().nextInt();
      case "UNIFORM":
      default:
        return RandomHelper.getUniform().nextInt();
    }
  }
}
