package ds2project;

import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;


public class Relay3Unicast extends Relay3 {
	protected int nUpcall = 0;

	public Relay3Unicast(ContinuousSpace<Object> space, int id, Network<Object> graph) {
		super(space, id, graph);
	}

	@Override
	public void onSense(Perturbation p) {
		this.recordTotalPerturbation(p);
		if (log.get(p.getSrc()) == null)
			log.put(p.getSrc(), new LinkedList<>());

		if (p instanceof ARQ) {
			onSenseARQ(p);
		} else {
			LinkedList<Perturbation> node_log = log.get(p.getSrc());
			int next;
			if (node_log.isEmpty())
				next = 0;
			else
				next = next_ref(node_log.getLast());
			if (next == p.getRef()) {
				forward(p);
				node_log.add(p);
				if (p.getVal().split(";")[0].equals(String.valueOf(getId()))) {
					this.logPerturbation(p);
					this.updateAvgLatency(p.generationTick);
					nUpcall++;
				}
			}
		}
	}

	@Override
	public void scheduledSend() {
		if (!log.isEmpty()) {
			Random r = new Random();
			Relay1 randomDestination = (Relay1) log.keySet().toArray()[r.nextInt(log.size())];
			Perturbation p = new Perturbation(this, lastRef, randomDestination.getId() + ";Message" + lastRef);
			lastRef++;
			forward(p);
		}
	}

	// Simulate a discovery message sent to neighbors
	@ScheduledMethod(start=1, interval=50000)
	public void scheduledDiscovery() {
		for (Object o : graph.getAdjacent(this)) {
			Relay3Unicast dest = (Relay3Unicast) o;
			if (log.get(dest) == null)
				log.put(dest, new LinkedList<>());
			dest.receiveDiscovery(log.keySet());
		}
	}
	
	// Upon reception of a discovery message, add src to the known nodes
	public void receiveDiscovery(Set<Relay1> nodes) {
		for (Relay1 o : nodes) {
			Relay3Unicast n = (Relay3Unicast) o;
			if (log.get(n) == null)
				log.put(n, new LinkedList<>());
		}
	}
}
