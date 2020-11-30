package ds2project;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.hamcrest.core.IsInstanceOf;

import bibliothek.gui.dock.util.Priority;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

public class Relay3 extends Relay1 {
	protected Map<Relay1, LinkedList<Perturbation>> log = new HashMap<>();

	public Relay3(ContinuousSpace<Object> space, int id, Network<Object> graph) {
		super(space, id, graph);
		double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		RunEnvironment.getInstance().getCurrentSchedule()
				.schedule(ScheduleParameters.createUniformProbabilityRepeating(currentTick + 1000, currentTick + 10000,
						10000, 20000, ScheduleParameters.RANDOM_PRIORITY), this, "sendARQ");
	}

	@Override
	public void onSense(Perturbation p) {
		this.recordTotalPerturbation(p);
		if (p instanceof ARQ) {
			this.logPerturbation(p);
			onSenseARQ(p);
		} else {
			if (log.get(p.getSrc()) == null)
				log.put(p.getSrc(), new LinkedList<>());

			LinkedList<Perturbation> node_log = log.get(p.getSrc());
			int next;
			if (node_log.isEmpty())
				next = 0;
			else
				next = next_ref(node_log.getLast());
			if (next == p.getRef()) {
				this.logPerturbation(p);
				if (!(p instanceof ARQReply))
					this.updateAvgLatency(p.generationTick);
				
				forward(p);
				node_log.add(p);
			}
		}
	}

	protected void onSenseARQ(Perturbation p) {
		if (log.get(p.getSrc()) == null)
			log.put(p.getSrc(), new LinkedList<>());
		else
			for (Perturbation q : log.get(p.getSrc()))
				if (q.getRef() == p.getRef())
					forward(new ARQReply(q));
	}

	public void sendARQ() {
		for (Relay1 n : log.keySet()) {
			LinkedList<Perturbation> node_log = log.get(n);
			if (!node_log.isEmpty()) {
				ARQ req = new ARQ(n, next_ref(node_log.getLast()));
				forward(req);
				Logger.logSentARQ(this, req);
			} else {
				ARQ req = new ARQ(n, 0);
				forward(req);
				Logger.logSentARQ(this, req);
			}
		}
	}

	public int getnNodes() {
		return log.size();
	}

	public int getnPerturbations() {
		int sum = 0;
		for (Relay1 key : log.keySet())
			sum += log.get(key).size();
		return sum;
	}
}
