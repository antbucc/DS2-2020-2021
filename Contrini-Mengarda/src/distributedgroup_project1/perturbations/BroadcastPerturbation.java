package distributedgroup_project1.perturbations;

import distributedgroup_project1.messages.BroadcastMessage;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class BroadcastPerturbation extends Perturbation {

	/**
	 * Constructor of the BroadcastPerturbation class.
	 * 
	 * @param space:   the continuous space where the perturbation will be displayed
	 * @param grid:    the grid where the perturbation will be located
	 * @param source:  the ID of the relay that has initially created the perturbation
	 * @param ref:     it identifies a perturbation among many with the same source
	 * @param message: the message that is carried by the perturbation
	 */
	public BroadcastPerturbation(ContinuousSpace<Object> space, Grid<Object> grid, int source, int ref,
			BroadcastMessage message) {
		super(space, grid, source, ref, message);
	}

	/**
	 * Method to clone the existing perturbation.
	 * 
	 * @return the copy of the perturbation
	 */
	@Override
	public Perturbation clone() {
		return new BroadcastPerturbation(space, grid, source, ref, (BroadcastMessage) message);
	}

}
