package distributedgroup_project1.perturbations;

import distributedgroup_project1.messages.Message;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class RetransmissionPerturbation extends Perturbation {

	/**
	 * Constructor of the RetransmissionPerturbation class.
	 * 
	 * @param space:   the continuous space where the perturbation will be displayed
	 * @param grid:    the grid where the perturbation will be located
	 * @param source:  the ID of the relay that has initially created the perturbation
	 * @param ref:     it identifies a perturbation among many with the same source
	 * @param message: the message that is carried by the perturbation
	 */
	public RetransmissionPerturbation(ContinuousSpace<Object> space, Grid<Object> grid, int source, int ref,
			Message message) {
		super(space, grid, source, ref, message);
	}

	/**
	 * Method to clone the existing perturbation.
	 * 
	 * @return the copy of the perturbation
	 */
	@Override
	public Perturbation clone() {
		return new RetransmissionPerturbation(space, grid, source, ref, message);
	}

	/**
	 * Method that transforms a perturbation of class Perturbation to
	 * RetransmissionPerturbation
	 * 
	 * @param perturbation: the perturbation to be converted
	 * @return: the new RetransmissionPerturbation perturbation
	 */
	public static RetransmissionPerturbation from(Perturbation perturbation) {
		return new RetransmissionPerturbation(perturbation.space, perturbation.grid, perturbation.source,
				perturbation.ref, perturbation.message);
	}

}
