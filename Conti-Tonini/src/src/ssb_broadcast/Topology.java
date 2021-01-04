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
	
	public Topology(int gridSize) {
		this.gridSize = gridSize;
	}
	
	public abstract List<NdPoint> getPoints(int count);

	public abstract NdPoint getPoint();
	
	public int getGridSize() {
		return this.gridSize;
	}
}