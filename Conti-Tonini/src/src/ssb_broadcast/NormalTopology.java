package ssb_broadcast;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import repast.simphony.space.continuous.NdPoint;

/**
 * Initializes a random topology where Observers
 * are distributed from the center of the space following a normal distribution
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class NormalTopology extends Topology {
	public NormalTopology(int gridSize, int observersCount) {
		super(gridSize, observersCount);
	}

	@Override
	public List<NdPoint> getPoints() {
		List<NdPoint> points = new ArrayList<>();
		Normal normal = new Normal(0.0, 0.25, RandomEngine.makeDefault());
		
		double offset = this.getGridSize() / 2;
		
		for (int i = 0; i < this.getObserversCount(); i++) {
			NdPoint newPoint;
			
			do {
				double x = normal.nextDouble() * offset + offset;
				double y = normal.nextDouble() * offset + offset;
				
				newPoint = new NdPoint(x, y);
			} while (newPoint == null || points.contains(newPoint));
			
			points.add(newPoint);
		}
		
		return points;
	}
}
