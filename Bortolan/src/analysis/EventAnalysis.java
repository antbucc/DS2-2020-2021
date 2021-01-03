package analysis;

import ssbgossip.Event;
import ssbgossip.GossipBuilder;
import ssbgossip.Participant;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;

public class EventAnalysis {
	Map<Event, EventStatistics> es;
	Queue<EventStatistics> lastStats;
	Context<Object> context;

	public EventAnalysis(Context<Object> context) {
		this.es = new HashMap<>();
		this.lastStats = new LinkedList<>();
		this.context = context;
	}

	public void created(Event e, Participant p) {
		EventStatistics stats = new EventStatistics();
		context.add(stats);
		lastStats.add(stats);
		if (lastStats.size() > GossipBuilder.participants
				* this.getEventProb() * 500) {
			lastStats.poll();
		}
		es.put(e, stats);
		stats.received(p);
	}

	public void received(Event e, Participant p) {
		EventStatistics stats = es.get(e);
		stats.received(p);
	}

	public void addMessages(Event e, int messages) {
		EventStatistics stats = es.get(e);
		stats.addMessages(messages);
	}

	public double getAverageDeliveryRate() {
		double dr = 0;
		for (EventStatistics stats : lastStats) {
			dr += stats.getDeliveryRate();
		}
		return dr / lastStats.size();
	}

	public double getMaxLatency() {
		double maxLatency = 0;
		for (EventStatistics stats : lastStats) {
			double latency = stats.getMaxLatency();
			if (latency > maxLatency) {
				maxLatency = latency;
			}
		}
		return maxLatency;
	}
	
	public double getTotalEvents(){
		return es.size();
	}

	public int nodesNumber() {
		return GossipBuilder.participants;
	}

	private double getEventProb() {
		return (double) RunEnvironment.getInstance().getParameters()
				.getValue("event_probability");
	}
}
