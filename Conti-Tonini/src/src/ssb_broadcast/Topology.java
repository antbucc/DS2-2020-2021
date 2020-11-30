package ssb_broadcast;

import java.util.List;

import repast.simphony.space.continuous.NdPoint;

/**
 * Defines how each Observer should be placed
 * in the simulation
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public abstract class Topology {	
	private final int gridSize;
	private final int observersCount;
	
	public Topology(int gridSize, int observersCount) {
		this.gridSize = gridSize;
		this.observersCount = observersCount;
	}
	
	public abstract List<NdPoint> getPoints();

	public int getGridSize() {
		return this.gridSize;
	}
	
	public int getObserversCount() {
		return this.observersCount;
	}
}

