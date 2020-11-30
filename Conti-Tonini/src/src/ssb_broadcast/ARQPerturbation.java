package ssb_broadcast;

import java.util.List;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

/**
 * Represents an ARQ Perturbation that carries
 * ARQ request Payloads
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class ARQPerturbation extends Perturbation {
	public ARQPerturbation(ContinuousSpace<Object> space, Grid<Object> grid, List<Payload> payloads) {
		super(space, grid, payloads);
	}
}
