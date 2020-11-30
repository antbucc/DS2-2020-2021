package ds2project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;
import repast.simphony.query.Query;


public class Relay1 {
	public int id;
	public Boolean failed = false;
	protected double distance;
	protected Map<Relay1, Integer> frontier = new HashMap<>();
	protected ContinuousSpace<Object> space;
	protected double propTime;
	protected Network<Object> graph;
	protected int lastRef = 0;
	protected double avgLatency = 0;
	protected int discardedPerturbations=0;
	protected int sentPerturbations=0;

	public Relay1(ContinuousSpace<Object> space, int id, Network<Object> graph) {
		this.space = space;
		this.id = id;
		this.graph = graph;
		
		// Retrieval of simulation parameters
		Parameters params = RunEnvironment.getInstance().getParameters();
		distance = params.getDouble("BroadcastDistance");
		propTime = params.getDouble("propTime");
		double meanInterval = params.getDouble("avgSendingTime");
		double sdInterval = params.getDouble("sigmaSendingTime");
		RunEnvironment.getInstance().getCurrentSchedule().schedule(
			ScheduleParameters.createNormalProbabilityRepeating(
				RunEnvironment.getInstance().getCurrentSchedule().getTickCount() + meanInterval,
				1000,
				meanInterval,
				sdInterval,
				ScheduleParameters.RANDOM_PRIORITY
			),
			this,
			"scheduledSend"
		);
	}

	public void onSense(Perturbation p) {
		this.recordTotalPerturbation(p);
		// If src is unknown, create a new expectation for ref 0
		if (frontier.get(p.getSrc()) == null)
			frontier.put(p.getSrc(), Integer.valueOf(0));
		
		// If ref has the expected value
		if (frontier.get(p.getSrc()) == p.getRef()) {
			this.logPerturbation(p);
			this.updateAvgLatency(p.generationTick);
			forward(p);
			frontier.put(p.getSrc(), next_ref(p));
		}
	}
	
	protected void logPerturbation(Perturbation p) {
		Logger.recordPerturbation(
			this,
			p,
			RunEnvironment.getInstance().getCurrentSchedule().getTickCount()
		);
	}
	
	protected void recordTotalPerturbation(Perturbation p) {
		Logger.totalPerturbations(
				this,
				p,
				RunEnvironment.getInstance().getCurrentSchedule().getTickCount()
			);
	}

	protected void updateAvgLatency(double newVal) {
		double alpha = 0.1;
		double curr_ticks = RunEnvironment.getInstance().getCurrentSchedule().getTickCount(); 
		avgLatency = (curr_ticks - newVal)*alpha + avgLatency*(1-alpha);
	}
	
	protected void forward(Perturbation p) {
		for (RepastEdge<Object> e : graph.getEdges(this)) {
			CustomEdge<Object> edge = (CustomEdge<Object>) e;
			// give the perturbation to the edge
			sentPerturbations++;
			edge.take(p, this);
		}
	}

	public void fail() {
		System.out.println("Node " + getId() + " setting state to failed.");
		this.failed = true;
		
		ArrayList<RepastEdge<Object>> edges = new ArrayList();
		for (RepastEdge e : graph.getEdges(this))
			edges.add(e);
		for (RepastEdge e : edges)
			graph.removeEdge(e);
	}

	protected Integer next_ref(Perturbation p) {
		if (p == null)
			return Integer.valueOf(0);
		else
			return Integer.valueOf(p.getRef().intValue() + 1);
	}

	public int getId() {
		return id;
	}

	@ScheduledMethod(start = 1)
	public void linkNearby() {
		// find actors within Broadcast Distance
		Query<Object> query = new ContinuousWithin<Object>(ContextUtils.getContext(this), this, distance);
		for (Object o : query.query()) {
			Relay1 n = (Relay1) o;
			
			// If I've not been added to any edge already
			if (graph.getEdge(this, n) == null && !n.failed) {
				CustomEdge<Object> edge = (CustomEdge<Object>) graph.addEdge(this, n);
				double distance = space.getDistance(space.getLocation(this), space.getLocation(n)); 
				edge.setWeight(distance*propTime);
			}
		}
	}

	public void scheduledSend() {
		Perturbation p = new Perturbation(this, lastRef, "Message" + lastRef);
		lastRef++;
		forward(p);
	}

	public double getAvgLatency() {
		return avgLatency;
	}
	
	public void discarded() {
		discardedPerturbations++;
	}

	public double getDiscardedPerturbationsRatio() {
		if(sentPerturbations==0)
			return 0;
		return ((double)discardedPerturbations)/sentPerturbations;
	}
	
	public boolean isFailed() {
		return failed;
	}
}
