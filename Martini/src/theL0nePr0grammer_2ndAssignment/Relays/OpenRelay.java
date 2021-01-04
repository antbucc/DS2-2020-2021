package theL0nePr0grammer_2ndAssignment.Relays;

import theL0nePr0grammer_2ndAssignment.Logs.Store;

public class OpenRelay extends Relay{

	public OpenRelay(int id, double x, double y, double latency, double jitter, double perturbationProb, double spawnProb, double leaveProb, double lossProb) {
		super(id, x, y, latency, jitter, perturbationProb, spawnProb, leaveProb, lossProb);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onSense(Object p) {
		Store rec = (Store) p;
		this.newEvents+=this.store.update(rec);
	}
    
	public boolean compareStatus(Relay r) {
    	for (Relay k : r.store.getKeys()) {
    		if (this.store.getLog(k) == null || r.store.getLog(k).last().getIndex() != this.store.getLog(k).last().getIndex())
    			return false;
    	}
    	return true;
    }
    
    public void printStatus() {
    	System.out.println(this);
    	this.store.getKeys().forEach(k -> {
    		if (this.store.getLog(k).last() != null) 
    			System.out.println("\t" + k + ": " +this.store.getLog(k).last().getIndex());
    		else
    			System.out.println("\t" + k + ": null");
        	
    	});
    	System.out.println();
    }
}
