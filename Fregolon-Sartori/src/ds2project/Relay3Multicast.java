package ds2project;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;


public class Relay3Multicast extends Relay3 {
	private int nUpcall = 0;
	ArrayList<String> mySubscriptions = new ArrayList<String>();

	public Relay3Multicast(ContinuousSpace<Object> space, int id, Network<Object> graph) {
		super(space, id, graph);
		Random r = new Random();
		for(int i=0; i<10; i++) {
			if(r.nextFloat()<0.4) {
				mySubscriptions.add(String.valueOf(i));
			}
		}
	}

	@Override
	public void onSense(Perturbation p) {
		this.recordTotalPerturbation(p);
		if (p instanceof ARQ) {
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
				forward(p);
				node_log.add(p);
				if (mySubscriptions.contains(p.getVal().split(";")[0])) {
					nUpcall++;
					this.logPerturbation(p);
					this.updateAvgLatency(p.generationTick);
				}
			}
		}
	}

	public void addSubscription(String id) {
		mySubscriptions.add(id);
	}

	public void removeSubscription(String id) {
		mySubscriptions.remove(id);
	}

	@Override
	public void scheduledSend() {
		if (!mySubscriptions.isEmpty()) {
			Random r = new Random();
			Perturbation p = new Perturbation(
				this,
				lastRef,
				mySubscriptions.get(r.nextInt(mySubscriptions.size())) + ";Message" + lastRef
			);
			lastRef++;
			forward(p);
		}
	}
}
