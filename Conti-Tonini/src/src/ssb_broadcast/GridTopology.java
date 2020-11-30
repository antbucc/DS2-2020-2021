package ssb_broadcast;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.collections.Pair;

/**
 * Initializes a random topology where Observers
 * are equally distributed in a grid fashion
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class GridTopology extends Topology {

	public GridTopology(int gridSize, int observersCount) {
		super(gridSize, observersCount);
	}

	@Override
	public List<NdPoint> getPoints() {
		List<NdPoint> points = new ArrayList<>();
		
		double cellDimension = this.getGridSize() * 1.0 / this.getSplits();
		double cellOffset = Math.ceil(cellDimension) / 2;
		
		for (int i = 0; i < this.getObserversCount(); i++) {
			NdPoint newPoint;
			
			do {
				Pair<Integer, Integer> coords = this.getObserverCoords(i);
				
				double x = coords.getFirst() * cellDimension + cellOffset;
				double y = coords.getSecond() * cellDimension + cellOffset;
				
				newPoint = new NdPoint(x, y);
			} while (newPoint == null || points.contains(newPoint));
			
			points.add(newPoint);
		}
		
		return points;
	}
	
	private int getSplits() {
		return (int)Math.ceil(Math.sqrt(this.getObserversCount()));
	}
	
	private Pair<Integer, Integer> getObserverCoords(int index) {
		int colIndex = index / this.getSplits();
		int rowIndex = index % this.getSplits();
		
		return new Pair<Integer, Integer>(rowIndex, colIndex);
	}
}
