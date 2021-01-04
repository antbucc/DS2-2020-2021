package ds2.utility;

// Repast libraries
import repast.simphony.random.RandomHelper;

public final class RandomNormal {
	/**
	 * Get a random variable using a normal distribution with the specified mean and variance
	 * @param mean The mean to be used
	 * @param var The variance to be used
	 * @return The random number
	 */
	public static double random(double mean, double var) {
		return RandomHelper.createNormal(mean, var)
		 				   .apply(RandomHelper.nextDoubleFromTo(0, 1));
	}

	/**
	 * Get a random variable using a normal distribution with the specified mean and variance not less then the specified minimum
	 * @param mean The mean to be used
	 * @param var The variance to be used
	 * @param min The minimum under which the result cannot go
	 * @return The random number
	 */
	public static double random(double mean, double var, double min) {
		double r = RandomNormal.random(mean, var);
		
		return r<min? min : r;
	}
}
