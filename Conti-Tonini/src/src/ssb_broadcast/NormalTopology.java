  
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
	private final Normal normal;
	private final double offset;
	
	public NormalTopology(int gridSize) {
		super(gridSize);
		
		this.normal = new Normal(0.0, 0.25, RandomEngine.makeDefault());
		this.offset = this.getGridSize() / 2;
	}

	@Override
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
		double x = normal.nextDouble() * offset + offset;
		double y = normal.nextDouble() * offset + offset;
		
		return new NdPoint(x, y);
	}
}