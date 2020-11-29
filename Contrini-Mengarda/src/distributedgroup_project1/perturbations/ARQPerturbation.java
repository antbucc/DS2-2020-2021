package distributedgroup_project1.perturbations;

import distributedgroup_project1.messages.ARQMessage;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class ARQPerturbation extends Perturbation {
	
	/**
	 * Constructor of the ARQPerturbation class.
	 * 
	 * @param space:   the continuous space where the perturbation will be displayed
	 * @param grid:    the grid where the perturbation will be located
	 * @param source:  the ID of the relay that has initially created the perturbation
	 * @param ref:     it identifies a perturbation among many with the same source
	 * @param message: the message that is carried by the perturbation
	 */
	public ARQPerturbation(ContinuousSpace<Object> space, Grid<Object> grid, int source, int ref, ARQMessage message) {
		super(space, grid, source, ref, message);
	}

	/**
	 * Method to clone the existing perturbation.
	 * 
	 * @return the copy of the perturbation
	 */
	@Override
	public Perturbation clone() {
		return new ARQPerturbation(space, grid, source, ref, (ARQMessage) message);
	}
	
}
