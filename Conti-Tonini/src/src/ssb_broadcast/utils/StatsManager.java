package ssb_broadcast.utils;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.collections.Pair;
import ssb_broadcast.Payload;

public class StatsManager {
	private final int id;
	
	private int createdPerturbations = 0; // # perturbations created
	private int forwardedPerturbations = 0; // # perturbations forwarded 
	private int arqReplyPerturbations = 0; // # perturbations of arq replies
	// total sent = created + forwarded + arqReply
	// avg. perturbations per payload = created / total sent
	
	private int receivedPerturbations = 0; // sum of received perturbations
	private int receivedArqRequestPerturbations = 0;
	// total received = received + arqRequests
	// avg. perturbations received = received / total received
	
	private List<Pair<Payload, Integer>> delays = new ArrayList<>(); // payload with receive delay
	
	public StatsManager(int id) {
		this.id = id;
	}
	
	public int getCreatedPerturbations() {
		return this.createdPerturbations;
	}
	
	public void incrementCreatedPerturbations() {
		this.createdPerturbations++;
	}
	
	public int getForwardedPerturbations() {
		return this.forwardedPerturbations;
	}
	
	public void incrementForwardedPerturbations() {
		this.forwardedPerturbations++;
	}
	
	public int getArqReplyPerturbations() {
		return this.arqReplyPerturbations;
	}
	
	public void incrementArqReplyPerturbations() {
		this.arqReplyPerturbations++;
	}
	
	public int getReceivedPerturbations() {
		return this.receivedPerturbations;
	}
	
	public void incrementReceivedPerturbations() {
		this.receivedPerturbations++;
	}
	
	public int getReceivedArqRequestPerturbations() {
		return this.receivedArqRequestPerturbations;
	}
	
	public void incrementReceivedArqRequestPerturbations() {
		this.receivedArqRequestPerturbations++;
	}
	
	public void addDelay(Payload payload, int delay) {
		this.delays.add(new Pair<>(payload, delay));
	}
	
	public void save() {
		String filename = "observer_" + this.id;
		
		CSVHelper.writeDelays(filename, delays);
	}
}
