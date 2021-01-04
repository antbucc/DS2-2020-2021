package theL0nePr0grammer_2ndAssignment.Relays;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;
import theL0nePr0grammer_2ndAssignment.Logs.Store;

public class TransientRelay extends Relay{

	private List<Relay> followed;
	private List<Relay> blocked;
	
	public TransientRelay(int id, double x, double y, double latency, double jitter, double perturbationProb, double spawnProb, double leaveProb, double lossProb) {
		super(id, x, y, latency, jitter, perturbationProb, spawnProb, leaveProb, lossProb);
		this.followed = new ArrayList<Relay>();
		this.blocked = new ArrayList<Relay>();
	}

	@Override
	public void onSense(Object p) {
		Store copy = (Store) p;
		Store rec = new Store(copy);

		rec.getKeys().forEach(k -> {
			if (!this.followed.contains(k) && !this.blocked.contains(k)) {
				//if this is not already followed, we can decide wheather to follow it or not
				//with and arbitrary choice of 0.7. This is fixed, as in a real context there should
				//exist some policies to take this decision
				
				if (RandomHelper.nextDoubleFromTo(0.0, 1.0) > (0.8) && !k.equals(this)) {
        			this.blocked.add(k);
					Network<Object> net = (Network<Object>) ContextUtils.getContext(this).getProjection("blockNet");
        			
        			net.addEdge(this, k);
				}
					
				else {
					this.followed.add(k);
        			Network<Object> net = (Network<Object>) ContextUtils.getContext(this).getProjection("followNet");
        			
        			net.addEdge(this, k);
				}
					
			}
		});
		
		//remove each blocked
		blocked.forEach(b -> {
			rec.removeKey(b);
		});
		
		this.newEvents+=this.store.update(rec);
	}

	@Override
	public boolean compareStatus(Relay r) {
    	for (Relay k : this.followed) {
    		if (r.store.containsKey(k))
    			if (r.store.getLog(k).last().getIndex() != this.store.getLog(k).last().getIndex())
    				return false;
    	}
    	return true;
	}

	@Override
	public void printStatus() {
		System.out.println(this);
		System.out.println("\tFollowing:");
    	this.followed.forEach(k -> {
    		if (this.store.getLog(k).last() != null) 
    			System.out.println("\t" + k + ": " +this.store.getLog(k).last().getIndex());
    		else
    			System.out.println("\t" + k + ": null");
        	
    	});
    	System.out.println("\tBlocked:");
    	this.blocked.forEach(k -> {
    		System.out.println("\t" + k);
        	
    	});
    	System.out.println();
	}

}
