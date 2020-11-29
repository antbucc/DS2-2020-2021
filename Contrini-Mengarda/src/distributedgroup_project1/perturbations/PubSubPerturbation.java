package distributedgroup_project1.perturbations;

import distributedgroup_project1.messages.PubSubMessage;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class PubSubPerturbation extends Perturbation {
	
	/**
	 * Constructor of the PubSubPerturbation class.
	 * 
	 * @param space:   the continuous space where the perturbation will be displayed
	 * @param grid:    the grid where the perturbation will be located
	 * @param source:  the ID of the relay that has initially created the perturbation
	 * @param ref:     it identifies a perturbation among many with the same source
	 * @param message: the message that is carried by the perturbation
	 */
	public PubSubPerturbation(ContinuousSpace<Object> space, Grid<Object> grid, int source, int ref,
			PubSubMessage message) {
		super(space, grid, source, ref, message);
	}

	/**
	 * Method to clone the existing perturbation.
	 * 
	 * @return the copy of the perturbation
	 */
	@Override
	public Perturbation clone() {
		return new PubSubPerturbation(space, grid, source, ref, (PubSubMessage) message);
	}

}
