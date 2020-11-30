package broadcastOnlyCommunication;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public abstract class Relay {
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	private List<Perturbation> processedPerturbations = new ArrayList<>(); // used for log purposes
	private String id;
	/* List of the perturbations that we have to send */
	protected List<Perturbation> perturbationsToSend = new ArrayList<>();
	/* Bytes remaining to send for the current perturbation */
	protected int missingBytes;
	/* Connection configurations */
	private int packetSize;
	private int transmissionSpeed;

	public Relay(ContinuousSpace<Object> space, Grid<Object> grid, String id) {
		this.space = space;
		this.grid = grid;
		this.id = id;
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		this.packetSize = params.getInteger("packetSize");
		this.transmissionSpeed = params.getInteger("transmissionSpeed");
		
		this.missingBytes = packetSize;
	}

	public void onSense(Perturbation p) {
		this.processedPerturbations.add(p);
		processPerturbation(p);
	}
	
	public abstract void processPerturbation(Perturbation p);

	public void forwardPerturbation(Perturbation p) {
		perturbationsToSend.add(p);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void sendPerturbation() {
		if(perturbationsToSend.size() > 0) {
			if (missingBytes <= 0) {
				// I've sent all the bytes of the perturbation				
				var p = perturbationsToSend.remove(0);
				Utils.forwardCopyOfPerturbation(grid, p, this);
				
				missingBytes = packetSize;
			}
			else {
				// A sending of a perturbation is going on
				missingBytes -= transmissionSpeed;
			}
		}
	}

	public static int nextRef(Perturbation p) {
		return p.ref + 1; // for sequence numbers
		// return hash(P) // for hash chaining
	}

	public String getRelayId() {
		return this.id;
	}

	public String getArrivedPerturbations() {
		var arrived = new ArrayList<String>();
		for (var pert : processedPerturbations) {
			if (!pert.val.equals(RelayIII.ARQ_VAL) && // avoid ARQ request
				!(pert.isUnicastMessage() && !pert.receiverId.equals(id)) && // avoid unicast where I'm not the receiver
				!(pert.isTopicMessage() && !((RelayIII)this).subscribedTopics.contains(pert.getTopic()))) { // avoid topic where I'm not subscribed
				arrived.add(pert.senderId + " " + pert.ref);
			}
		}

		processedPerturbations.clear();

		return String.join(", ", arrived);
	}

	@Override
	public String toString() {
		return "Relay [id=" + id + "]";
	}
	
	public String getId() {
		return id;
	}
	
}
