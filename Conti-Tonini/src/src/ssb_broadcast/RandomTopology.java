package ssb_broadcast;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;

/**
 * Initializes a random topology where Observers
 * are distributed in the space through a uniform distribution
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class RandomTopology extends Topology {
	public RandomTopology(int gridSize, int observersCount) {
		super(gridSize, observersCount);
	}

	@Override
	public List<NdPoint> getPoints() {
		List<NdPoint> points = new ArrayList<>();
		
		for (int i = 0; i < this.getObserversCount(); i++) {
			NdPoint newPoint;
			
			do {
				double x = RandomHelper.getUniform().nextDoubleFromTo(0, this.getGridSize());
				double y = RandomHelper.getUniform().nextDoubleFromTo(0, this.getGridSize());
				
				newPoint = new NdPoint(x, y);
			} while (newPoint == null || points.contains(newPoint));
			
			points.add(newPoint);
		}
		
		return points;
	}
	
}