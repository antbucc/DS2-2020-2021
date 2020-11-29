package distributedgroup_project1.perturbations;

import java.util.HashSet;
import java.util.Set;

import distributedgroup_project1.messages.Message;
import distributedgroup_project1.Utils;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public abstract class Perturbation {

	protected final ContinuousSpace<Object> space;
	protected final Grid<Object> grid;
	
	// ID of the source relay that initially created this perturbation
	protected final int source;
	// The "ref" of this perturbation, unique within the same source
	protected final int ref;
	// The message carried by this perturbation
	protected final Message message;
	
	// This Set is used to remember which relays have already sensed this perturbation
	private final Set<Integer> processedInRelays = new HashSet<>();

	// The runtime tick when this perturbation was created and sent
	private final double creationTick;

	// The radius of the perturbation in the space, determining how much it has spread so far
	private double radius;
	// The speed the perturbation should be enlarged with time
	private double spreadSpeed;

	/**
	 * Constructor of the Perturbation class.
	 * 
	 * @param space:   the continuous space where the perturbation will be displayed
	 * @param grid:    the grid where the perturbation will be located
	 * @param sender:  the relay that has created the perturbation
	 * @param ref:     it identifies a perturbation among many with the same source
	 * @param message: the message that is carried by the perturbation
	 */
	public Perturbation(ContinuousSpace<Object> space, Grid<Object> grid, int source, int ref, Message message) {
		this.space = space;
		this.grid = grid;
		this.source = source;
		this.ref = ref;
		this.message = message;
		this.creationTick = Utils.getCurrentTick();
		this.radius = 0;
		this.spreadSpeed = Utils.getParams().getDouble("spread_speed");
	}
	
	/**
	 * Method that increments the size of the perturbation at each tick.
	 */
	@ScheduledMethod(start = 0, interval = 1)
	public void propagate() {
		this.radius += this.spreadSpeed;
		
		// When the perturbation exceeds the maximum reach, it disappears
		if (this.radius > Utils.MAX_PERTURBATION_RADIUS) {
			Context<Object> context = Utils.getContext(this);
			context.remove(this);
		}
	}

	/**
	 * Method that returns the current radius of the perturbation.
	 * 
	 * @return the radius of the perturbation
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Method that returns the source of the perturbation.
	 * 
	 * @return the source of the perturbation
	 */
	public int getSource() {
		return source;
	}

	/**
	 * Method that returns the ref of the perturbation.
	 * 
	 * @return the ref of the perturbation
	 */
	public int getRef() {
		return ref;
	}

	/**
	 * Method that returns the message of the perturbation.
	 * 
	 * @return the message of the perturbation
	 */
	public Message getMessage() {
		return message;
	}
	
	/**
	 * Method to save the fact that the passed relay has already sensed and processed this perturbation.
	 * 
	 * @param relayId: the ID of the relay that has sensed the perturbation
	 */
	public void markAsProcessed(int relayId) {
		this.processedInRelays.add(relayId);
	}
	
	/**
	 * Method that returns true if the relay has already processed
	 * the perturbation.
	 * 
	 * @param relayId: the id of the relay that is sensing the perturbation 
	 * @return: true whether the relay has already processed the perturbation
	 */
	public boolean hasBeenProcessed(int relayId) {
		return this.processedInRelays.contains(relayId);
	}
	
	/**
	 * Method to provide the message size
	 * 
	 * @return: the size of the message, 0 if no message is present (ARQ)
	 */
	public int getMessageSize() {
		if (this.message == null) {
			return 0;
		} else {
			return this.message.getSize();
		}
	}

	/**
	 * Method to provide the creation tick of the perturbation
	 * 
	 * @return: the creation tick of the perturbation
	 */
	public double getCreationTick() {
		return creationTick;
	}
	
	/**
	 * Abstract method to clone the existing perturbation
	 */
	@Override
	public abstract Perturbation clone();
}
