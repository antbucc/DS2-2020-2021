package distributedgroup_project2.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import distributedgroup_project2.Utils;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;

public abstract class Relay {
	private final ContinuousSpace<Object> space;
	
	// ID of the relay.
	private final int id;
	// Incrementing counter of the next perturbation's "ref" ID.
	private int ref;
	// Flag that indicates whether a relay was created at the beginning of the simulation.
	private final boolean existsFromBeginning;
	
	// The log of received perturbations, for each source (ID, which is an Integer).
	private Map<Integer, List<Perturbation>> log = new HashMap<>();

	/**
	 * Constructor of the class Relay.
	 * 
	 * @param space: the continuous space where the relay will be displayed;
	 * @param id: unique field that identifies a relay;
	 * @param existsFromBeginning: true if the relay was created at the beginning of the simulation.
	 */
	public Relay(ContinuousSpace<Object> space, int id, boolean existsFromBeginning) {
		this.space = space;
		this.id = id;
		this.ref = 0;
		this.existsFromBeginning = existsFromBeginning;
	}

	/**
	 * Method to create and send a new perturbation, called periodically by the application layer.
	 * The perturbation is then appended to the logs and broadcasted.
	 */	
	public void broadcast(Object message) {
		Perturbation perturbation = new Perturbation(space, this.id, this.ref++, message);
		appendToLog(perturbation);
		sendPerturbation(perturbation);
	}

	/**
	 * Method to broadcast a perturbation in the continuous space.
	 * Broadcasting a perturbation in this implementation means placing the perturbation
	 * in the space, in the same position as the sender.
	 * The perturbation will then expand by itself with time and be intercepted by other relays.
	 * 
	 * @param perturbation: the perturbation to be sent.
	 */
	private void sendPerturbation(Perturbation perturbation) {
		Context<Object> context = Utils.getContext(this);
		context.add(perturbation);
		NdPoint spacePoint = space.getLocation(this);
		space.moveTo(perturbation, spacePoint.getX(), spacePoint.getY());
	}

	/**
	 * Method to append a perturbation to the local log of the relay.
	 * 
	 * @param perturbation: the perturbation to appended to the log
	 */
	private void appendToLog(Perturbation perturbation) {
		int source = perturbation.getSource();
		
		// The log is a Map keyed by source relay, so the first time we need to initialize
		// the list corresponding to the given source.
		if (!log.containsKey(source)) {
			log.put(source, new ArrayList<>());
		}
		
		log.get(source).add(perturbation);
	}

	/**
	 * Method to obtain the "ref" of the next perturbation that is expected from a given source.
	 * 
	 * @param source: the source for which the next ref is requested;
	 * @return the integer "ref" value that is expected.
	 */
	private int getNextRef(int source) {
		// If no perturbation has been received from the source,
		// the first perturbation (with ref=0) has still to be received.
		if (!log.containsKey(source)) {
			log.put(source, new ArrayList<>());
			return 0;
		}
		
		if (log.get(source).size() == 0) {
			return 0;
		}
		
		// Take the last perturbation that was logged and return its ref + 1.
		Perturbation lastReceived = log.get(source).get(log.get(source).size() - 1);
		return lastReceived.getRef() + 1;
	}

	/**
	 * Method to sense perturbations in the coninuous space.
	 * The method is automatically run every tick.
	 */
	@ScheduledMethod(start = 1, interval = 1)
	public void sensePerturbations() {
		// Retrieve all the agents within the sensing radius of the relay.
		ContinuousWithin<Object> within = new ContinuousWithin<>(this.space, this, Utils.MAX_PERTURBATION_RADIUS);
		CopyOnWriteArrayList<Object> perturbations = new CopyOnWriteArrayList<>();
		within.query().forEach(perturbations::add);
		
		perturbations.forEach(obj -> {
			// Process only agents that are perturbations.
			if (!(obj instanceof Perturbation)) {
				return;
			}
			
			Perturbation perturbation = (Perturbation) obj;
			
			// Do not process the perturbations that I sent or that I have already handled.
			// The second condition is needed because this method keeps getting called
			// even after the perturbation has reached (and went beyond) the relay.
			if (perturbation.getSource() == id || perturbation.hasBeenProcessed(this.id)) {
				return;
			}
			
			// Compute the distance between the relay and the circumference of the perturbation
			NdPoint pos1 = this.space.getLocation(perturbation);
			NdPoint pos2 = this.space.getLocation(this);
			double distance = this.space.getDistance(pos1, pos2) - perturbation.getRadius();
			
			if (distance <= 0) {
				// Memorize the fact that the perturbation has reached this particular relay
				perturbation.markAsProcessed(this.id);
				manageMessage(perturbation);
			}
		});
	}

	/**
	 * Method to handle a broadcast perturbation. If it is the expected
	 * perturbation, it is logged and forwarded.
	 * 
	 * @param perturbation: the perturbation sensed by the relay.
	 */
	private void manageMessage(Perturbation perturbation) {
		// If this is the perturbation that I'm expecting from this source.
		int nextRef = getNextRef(perturbation.getSource());

		// Check if it is the expected perturbation or if the relay has been added later
		// in the simulation and this is the first sensed perturbation from that source.
		if ((!this.existsFromBeginning && nextRef == 0) || perturbation.getRef() == nextRef) { 
			appendToLog(perturbation);
			forwardPerturbation(perturbation);
			
			// Deliver the perturbation/message.
			deliver(perturbation.getMessage());				
		}
	}	
	
	/**
	 * Method to forward a perturbation. The perturbation is cloned and then sent.
	 * 
	 * @param perturbation: the perturbation to be forwarded.
	 */
	private void forwardPerturbation(Perturbation perturbation) {
		Perturbation newPerturbation = perturbation.clone();
		sendPerturbation(newPerturbation);
	}

	/**
	 * Method to retrieve the id of the relay.
	 * 
	 * @return the id of the relay.
	 */
	public int getId() {
		return id;
	}

	
	/**
	 * Method called when a perturbation/message should be delivered to the application layer.
	 * 
	 * @param perturbation: the perturbation to deliver.
	 */
	public abstract void deliver(Object message);
}
