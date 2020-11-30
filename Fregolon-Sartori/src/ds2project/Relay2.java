package ds2project;

import java.util.ArrayList;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;


public class Relay2 extends Relay1 {
	protected ArrayList<Perturbation> bag;

	public Relay2(ContinuousSpace<Object> space, int id, Network<Object> graph) {
		super(space, id, graph);
		// this model adds a list of out of order perturbation
		bag = new ArrayList<Perturbation>();
	}

	@Override
	public void onSense(Perturbation p) {
		this.recordTotalPerturbation(p);
		if (frontier.get(p.getSrc()) == null)
			frontier.put(p.getSrc(), Integer.valueOf(0));
		// If ref is unseen and is >= than the last known
		if (p.getRef() >= frontier.get(p.getSrc()).intValue() && !bag.contains(p)) {
			bag.add(p);
			Perturbation toRemove = null;
			for (Perturbation pBag : bag) {
				if (pBag.getRef() == frontier.get(pBag.getSrc()).intValue()) {
					toRemove = pBag;
					this.logPerturbation(p);
					this.updateAvgLatency(p.generationTick);
					forward(pBag);
					frontier.put(pBag.getSrc(), next_ref(pBag));
				}
			}
			if(toRemove != null) {
				bag.remove(toRemove);
			}
		}
	}
}
