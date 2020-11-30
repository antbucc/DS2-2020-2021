package ssb_broadcast;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

/**
 * Represents a broadcast Perturbation that carries
 * Payloads
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class BroadcastPerturbation extends Perturbation {
	public BroadcastPerturbation(ContinuousSpace<Object> space, Grid<Object> grid, Payload payload) {
		super(space, grid, payload);
	}
}
