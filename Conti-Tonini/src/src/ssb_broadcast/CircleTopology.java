package ssb_broadcast;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.collections.Pair;

/**
 * Initializes a random topology where Observers
 * are equally distributed in a circle
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class CircleTopology extends Topology {
	public CircleTopology(int gridSize, int observersCount) {
		super(gridSize, observersCount);
	}

	@Override
	public List<NdPoint> getPoints() {
		List<NdPoint> points = new ArrayList<>();
		
		double offset = this.getGridSize() / 2 + 0.5;
		double radius = 3 * this.getGridSize() / 8;
		
		for (int i = 0; i < getObserversCount(); i++) {
			NdPoint newPoint;
			
			double angle = this.getSplits() * i;
			
			do {
				double x = radius * Math.cos(angle) + offset;
				double y = radius * Math.sin(angle) + offset;
				
				newPoint = new NdPoint(x, y);
			
				// System.out.format("New point: %s \n", newPoint.toString());
			} while (newPoint == null || points.contains(newPoint));
			
			// System.out.format("New final point: %s \n", newPoint.toString());
			
			points.add(newPoint);
		}
		
		return points;
	}
	
	public double getSplits() {
		return Math.toRadians(360.0 / this.getObserversCount());
	}

	public Pair<Double, Double> getCenter() {
		return new Pair<Double, Double>(this.getGridSize() * 1.0 / 2, this.getGridSize() * 1.0 / 2);
	}
}
