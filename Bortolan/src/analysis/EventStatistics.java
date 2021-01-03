package analysis;

import ssbgossip.GossipBuilder;
import ssbgossip.Participant;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;

public class EventStatistics {
	
	private double creationTime;
	private List<Participant> receivers;
	private double maxLatency = 0;
	private double latencySum = 0;
	private int sentMessages = 0;

	public EventStatistics() {
		this.creationTime = getTick();
		this.receivers = new ArrayList<>();
	}

	public void received(Participant p) {
		receivers.add(p);
		double latency = getTick() - creationTime;
		latencySum += latency;
		if (latency > maxLatency) {
			maxLatency = latency;
		}
	}

	public void addMessages(int messages) {
		sentMessages += messages;
	}

	public double getCreationTime() {
		return this.creationTime;
	}

	public double getDeliveryRate() {
		return ((double) receivers.size()) / GossipBuilder.participants;
	}

	public double getMaxLatency() {
		return maxLatency;
	}

	public double getAvgLatency() {
		return latencySum/receivers.size();
	}

	public int getSentMessages() {
		return sentMessages;
	}

	private double getTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
}
