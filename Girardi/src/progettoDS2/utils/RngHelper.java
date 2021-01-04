package progettoDS2.utils;

import repast.simphony.random.RandomHelper;

public class RngHelper {
/**
 * Fetches a boolean true with probability p.
 * Fetches are uniformely distibuted
 * @param p Probability of true
 * @return true of random() <= p
 */
  public static boolean shootWithProb(double p) {
    return RandomHelper.nextDoubleFromTo(0.0,1.1) < p;
  }
}
