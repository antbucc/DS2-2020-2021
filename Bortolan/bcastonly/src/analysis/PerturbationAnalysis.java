package analysis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import bcastonly.BcastOnlyBuilder;
import bcastonly.Perturbation;
import bcastonly.Relay;

/**
 * Collects statistics for all the perturbations sent during the execution.
 * Provide methods to get current estimations for average delivery rate and
 * maximum latency.
 * 
 * @author bortolan cosimo
 */
public class PerturbationAnalysis {
	Map<Perturbation, PerturbationStatistics> ps;
	Queue<PerturbationStatistics> lastStats;
	Context<Object> context;

	public PerturbationAnalysis(Context<Object> context) {
		this.ps = new HashMap<>();
		this.lastStats = new LinkedList<>();
		this.context = context;
	}

	public void sent(Perturbation p) {
		PerturbationStatistics stats = new PerturbationStatistics();
		context.add(stats);
		lastStats.add(stats);
		if (lastStats.size() > BcastOnlyBuilder.relay_count
				* this.getSendProb() * 200) {
			lastStats.poll();
		}
		ps.put(p, stats);
	}

	public void received(Perturbation p, Relay r) {
		PerturbationStatistics stats = ps.get(p);
		stats.received(r);
	}

	public void addMessages(Perturbation p, int messages) {
		PerturbationStatistics stats = ps.get(p);
		stats.addMessages(messages);
	}

	public double getAverageDeliveryRate() {
		double dr = 0;
		for (PerturbationStatistics stats : lastStats) {
			dr += stats.getDeliveryRate();
		}
		return dr / lastStats.size();
	}

	public double getMaxLatency() {
		double maxLatency = 0;
		for (PerturbationStatistics stats : lastStats) {
			double latency = stats.getMaxLatency();
			if (latency > maxLatency) {
				maxLatency = latency;
			}
		}
		return maxLatency;
	}

	public int nodesNumber() {
		return BcastOnlyBuilder.relay_count;
	}

	private double getSendProb() {
		return (double) RunEnvironment.getInstance().getParameters()
				.getValue("send_probability");
	}
}
