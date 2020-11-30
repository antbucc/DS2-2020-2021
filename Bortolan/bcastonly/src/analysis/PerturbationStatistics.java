package analysis;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import bcastonly.BcastOnlyBuilder;
import bcastonly.Relay;

/**
 * Collects statistics for a perturbation. Provide methods to get current
 * estimations for average delivery rate, maximum latency and sent messages.
 * 
 * @author bortolan cosimo
 */
public class PerturbationStatistics {

	private double startTime;
	private List<Relay> receivers;
	private double maxLatency = 0;
	private int sentMessages = 0;

	public PerturbationStatistics() {
		this.startTime = getTick();
		this.receivers = new ArrayList<>();
	}

	public void received(Relay relay) {
		receivers.add(relay);
		double latency = getTick() - startTime;
		if (latency > maxLatency) {
			maxLatency = latency;
		}
	}

	public void addMessages(int messages) {
		sentMessages += messages;
	}

	public double getStartTime() {
		return this.startTime;
	}

	public double getDeliveryRate() {
		return ((double) receivers.size()) / BcastOnlyBuilder.relay_count;
	}

	public double getMaxLatency() {
		return maxLatency;
	}

	public int getSentMessages() {
		return sentMessages;
	}

	private double getTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
}
