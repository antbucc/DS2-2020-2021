package ssb_broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import ssb_broadcast.Payload.PayloadType;
import ssb_broadcast.utils.StatsManager;

/**
 * Defines the Observer agent. This agent can emit and sense Payloads
 * carried by Perturbations.
 * 
 * The Observer contains an append-only log that stores every Payload received
 * and processed. The agent can query the log to reply ARQ requests received.
 * 
 * This class collects stats while the simulation is running. Please refer to
 * StatsManager.
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class Observer {
	// Defines the probability to drop a Payload received
	private final double dropProbability;
	
	// Defines the probability to send a Payload
	private final double sendProbability;
	
	// Uniquely identifies this Observer in the space
	private final int id;
	
	// Defines the continuous and grid objects of the 2D space
	private final ContinuousSpace<Object> space;
	private final Grid<Object> grid;
	
	// Append-only log structure
	private final Map<Integer, List<Payload>> log;
	
	// Stats collector
	private final StatsManager stats;
	
	// Number of observers in the space
	private final int observersCount;
	
	// Defines the type of communication of this simulation
	private final String communicationType;

	public Observer(int id, int observersCount, ContinuousSpace<Object> space, Grid<Object> grid) {
		Parameters params = RunEnvironment.getInstance().getParameters();

		this.id = id;
		this.space = space;
		this.grid = grid;
		this.log = new HashMap<>();
		this.stats = new StatsManager(id);
		this.dropProbability = params.getDouble("drop_probability");
		this.sendProbability = params.getDouble("send_probability");
		this.observersCount = observersCount;
		this.communicationType = params.getString("communication_type");
		
		// Initializes the append-only log
		for (int i = 0; i < observersCount; i++) {
			this.log.put(i, new ArrayList<>());
		}
	}

	public int getCreatedPerturbations() {
		return this.stats.getCreatedPerturbations();
	}
	
	public int getForwardedPerturbations() {
		return this.stats.getForwardedPerturbations();
	}
	
	public int getArqReplyPerturbations() {
		return this.stats.getArqReplyPerturbations();
	}
	
	public int getReceivedPerturbations() {
		return this.stats.getReceivedPerturbations();
	}
	
	public int getReceivedArqRequestPerturbations() {
		return this.stats.getReceivedArqRequestPerturbations();
	}
	
	public String getId() {
		return "" + this.id;
	}
	
	@ScheduledMethod(start = 1, interval = 200)
	public void run() {
		// Determine if the Observer should send a Payload now
		if (RandomHelper.getUniform().nextDouble() > this.sendProbability) {
			return;
		}
		
		int val = RandomHelper.getUniform().nextIntFromTo(1, 100);
		
		// Get the next reference value from the append-only log
		// We are guaranteed that the size of the id-th array of the log
		// represents the total number of messages sent by this Observer plus one
		int nextRef = this.log.get(this.id).size();
		
		Payload payload;
		if (this.communicationType.equals("p2p")) {
			// Find a destination for the Payload
			int destination = RandomHelper.getUniform().nextIntFromTo(0, this.observersCount);
			payload = new Payload(this.id, nextRef, val, destination);
			
			System.out.format("[%d] Message sent to %d\n", this.id, destination);
		} else {
			payload = new Payload(this.id, nextRef, val, PayloadType.BROADCAST);
		}
		
		this.stats.incrementCreatedPerturbations();
		
		// Add payload to log and forward it
		addToLog(payload);
		forward(payload);
	}
	
	@ScheduledMethod(start = 300, interval = 300)
	public void arq() {
		// System.out.format("[%d] Send ARQ \n", this.id);
		
		// Create a single ARQ request by appending all next refs for each
		// Observer in an array-like structure
		List<Payload> payloads = new ArrayList<>();
		for (Integer sourceId : this.log.keySet()) {
			if (sourceId == this.id) {
				continue;
			}
			
			int nextRef = getLastReference(sourceId) + 1;
			
			Payload payload = new Payload(sourceId, nextRef, 0, PayloadType.ARQ_REQUEST);
			payloads.add(payload);
		}
		
		forward(payloads);
	}
	
	@ScheduledMethod(start = 500, interval = 500)
	public void saveStats() {
		// Periodically save stats
		this.stats.save();
	}
	
	public void sense(Payload payload, int delay) {
		// Decide to drop a Payload to simulate loss of messages
		if (this.dropProbability != 0 && RandomHelper.getUniform().nextDouble() <= this.dropProbability) {
			return;
		}
		
		this.stats.addDelay(payload, delay);
		
		// Determines the type of request and act accordingly
		if (payload.getType() == PayloadType.ARQ_REQUEST) {
			this.stats.incrementReceivedArqRequestPerturbations();
			
			// Check if the requested message is available in the log
			int index = this.log.get(payload.getSource()).indexOf(payload);
			if (index != -1) {
				// Find the payload requested and forward it again
				Payload match = this.log.get(payload.getSource()).get(index);
				// System.out.format("[%d] Reply ARQ. ref %s source %s \n", this.id, reply.getRef(), reply.getSource());
				
				this.stats.incrementArqReplyPerturbations();
				
				Payload arqReply = new Payload(match.getSource(), match.getRef(), match.getVal(), PayloadType.ARQ_REPLY);
				forward(arqReply);
			}
			
			return;
		}
		
		this.stats.incrementReceivedPerturbations();
		
		// Duplicate suppression mechanism
		if (this.log.get(payload.getSource()).contains(payload)) {
			// System.out.format("[%d] Suppressing duplicate message from %d with ref %d - expected next ref %d \n", this.id, payload.getSource(), payload.getRef(), getLastReference(payload) + 1);

			return;
		}
		
		// Check if the Payload received is the one we expect.
		// If not, drop it. If the Payload is newer than the one
		// expected, an ARQ request will retrieve it in the
		// future
		if (isNextValidPayload(payload)) {
			this.stats.incrementForwardedPerturbations();
			
			// Add and forward
			addToLog(payload);
			forward(payload);
			
			// Log the delivery of a message in the case of point to point communication
			if (payload.getType() == PayloadType.POINT_TO_POINT && payload.getDestination() == this.id) {
				System.out.format("[%d] Message was delivered to destination\n", this.id);
			}
		}
	}
	
	// Forward and ARQ request
	private void forward(List<Payload> payloads) {
		// Get the simulation context
		Context<Object> context = ContextUtils.getContext(this);
		
		// Location of this observer
		GridPoint observerGridPoint = this.grid.getLocation(this);
		NdPoint observerNdPoint = this.space.getLocation(this);
		
		// Create a new perturbation
		Perturbation perturbation = new ARQPerturbation(space, grid, payloads);
		
		// Add the perturbation to the context and place it under the current observer
		context.add(perturbation);
		space.moveTo(perturbation, observerNdPoint.getX(), observerNdPoint.getY());
		grid.moveTo(perturbation, observerGridPoint.getX(), observerGridPoint.getY());
	}
	
	// Forward a broadcast message
	private void forward(Payload payload) {
		// Get the simulation context
		Context<Object> context = ContextUtils.getContext(this);
		
		// System.out.format("[%d] Forwarding message from source %d with ref %d \n", this.id, payload.getSource(), payload.getRef());
		
		// Location of this observer
		GridPoint observerGridPoint = this.grid.getLocation(this);
		NdPoint observerNdPoint = this.space.getLocation(this);
		
		// Create a new perturbation
		Perturbation perturbation = new BroadcastPerturbation(space, grid, payload);
		
		// Add the perturbation to the context and place it under the current observer
		context.add(perturbation);
		space.moveTo(perturbation, observerNdPoint.getX(), observerNdPoint.getY());
		grid.moveTo(perturbation, observerGridPoint.getX(), observerGridPoint.getY());
	}
	
	private void addToLog(Payload payload) {
		// Add payload to the log
		this.log.get(payload.getSource()).add(payload);
	}
	
	private int getLastReference(int sourceId) {
		int index = this.log.get(sourceId).size() - 1;
		
		// If no elements are available in the log, simply return 0
		// which is the ref of the first message
		if (index == -1) {
			return 0;
		}
		
		// Return the ref of the last message recorded for the sourceId
		return this.log.get(sourceId).get(index).getRef();
	}
	
	private boolean isNextValidPayload(Payload payload) {
		// System.out.format("[%d] %s \n", this.id, payload.toString());
		
		// First element check
		if (this.log.get(payload.getSource()).size() == 0) {
			return payload.getRef() == 0;
		}
		
		return getLastReference(payload.getSource()) + 1 == payload.getRef();
	}
}
