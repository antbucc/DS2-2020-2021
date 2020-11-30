package ds2.utility;

// Repast libraries
import repast.simphony.random.RandomHelper;

public final class RandomNormal {
	public static double random(double mean, double var) {
		return RandomHelper.createNormal(mean, var)
		 				   .apply(RandomHelper.nextDoubleFromTo(0, 1));
	}

	public static double random(double mean, double var, double min) {
		double r = RandomNormal.random(mean, var);
		
		return r<min? min : r;
	}
}
