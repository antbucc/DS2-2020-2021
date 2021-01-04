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
	public RandomTopology(int gridSize) {
		super(gridSize);
	}
	
	public List<NdPoint> getPoints(int count) {
		List<NdPoint> points = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			NdPoint newPoint;
			
			do {
				newPoint = getPoint();
			} while (newPoint == null || points.contains(newPoint));
			
			points.add(newPoint);
		}
		
		return points;
	}
	
	public NdPoint getPoint() {
		double x = RandomHelper.getUniform().nextDoubleFromTo(0, this.getGridSize());
		double y = RandomHelper.getUniform().nextDoubleFromTo(0, this.getGridSize());
		
		return new NdPoint(x, y);
	}
}
