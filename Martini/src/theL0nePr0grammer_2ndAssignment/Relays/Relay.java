package theL0nePr0grammer_2ndAssignment.Relays;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cern.jet.random.Normal;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;
import theL0nePr0grammer_2ndAssignment.Logs.Event;
import theL0nePr0grammer_2ndAssignment.Logs.Log;
import theL0nePr0grammer_2ndAssignment.Logs.Store;
import theL0nePr0grammer_2ndAssignment.Wavefronts.*;
import theL0nePr0grammer_2ndAssignment.Manager;

public abstract class Relay {
	protected int id;
	protected double x;
	protected double y;
	
	protected double perturbationProb;
	protected boolean finalPhase;
	
	//The space and the manager are declared, but not initialized, as they exist when the Relay is generated as RelayI, II or III.
	protected ContinuousSpace<Object> space;
	protected Manager aether;
	
	protected Map<Relay, Double> neighbours;
	protected List<Wavefront> incomingWavefronts;
	protected Store store;
	
	protected double spawnProb;
	protected double leaveProb;
	
	protected boolean active;
	protected double latency;
	protected double jitter;
	
	protected double lossProb;
	
	protected int newEvents;
	
	protected Normal distribution;
	
	public Relay(int id, double x, double y, double latency, double jitter, double perturbationProb, double spawnProb, double leaveProb, double lossProb) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.perturbationProb = perturbationProb;
		
		this.finalPhase = false;
		this.neighbours = new HashMap<Relay, Double>();
		this.incomingWavefronts = new ArrayList<Wavefront>();
		this.store = new Store();
		
		this.spawnProb = spawnProb;
		this.leaveProb = leaveProb;
		
		this.active = true;
		this.latency = latency;
		this.jitter = jitter;
		this.lossProb = lossProb;
		
		this.distribution = RandomHelper.createNormal(latency,jitter);
	}
	
	//STANDARD METHODS (EQUALS, HASHCODE, TO STRING)
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relay)) return false;
        Relay cmp = (Relay) o;
        return id == cmp.id;
    }
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
    @Override
    public String toString() { 
        return "(relay " + Integer.toString(this.id) + ")"; 
    }
    
    //GETTERS AND SETTERS
    public int getId() {
    	return this.id;
    }
    
    public double getX() {
    	return this.x;
    }
    
    public double getY() {
    	return this.y;
    }
    
    public void setX(double x) {
    	this.x = x;
    }
    
    public void setY(double y) {
    	this.y = y;
    }
    
    public double getPerturbationProb() {
    	return this.perturbationProb;
    }
    
    public double getLatency() {
    	return this.latency;
    }
    
    public boolean getActive() {
    	return this.active;
    }
    
    public void setActive(boolean value) {
    	this.active = value;
    }
    
	public void freeze() {
		this.finalPhase = true;
	}
    
	public double getMessageLife() {
		return this.distribution.nextDouble() / 2.0;
	}
	
	//VISUAL GETTERS
    public String getLabel() {
    	DecimalFormat df2 = new DecimalFormat("#.##");
    	return this.toString() + "\n" + df2.format(this.getX()) + ", " + df2.format(this.getY());
    }

    public double getR() {
       	if (this.perturbationProb != 0.0 && this.active)
    		return 0.0;
    	return 255.0;
    }
    public double getG() {
    	if (!this.active)
    		return 0.0;
    	return 255.0;
    }
    public double getB() {
    	return 0.0;
    }
    
    //AUXILIARY METHODS
	public void setContext(Manager aether, ContinuousSpace<Object> space) {
		this.aether = aether;
		this.space = space;
	}
	
	public void addNeighbour(Relay n, Double distance) {
		this.neighbours.put(n, distance);
	}

    public void addWavefront(Wavefront w) {
    	this.incomingWavefronts.add(w);
    }
    
	//ACTUAL RELAY IMPLEMENTATION
    
    @ScheduledMethod(start = 1, interval = 1)
    public void step() {
    	if (!finalPhase) {
        	if (RandomHelper.nextDoubleFromTo(0.0, 1.0) > (1.0-this.perturbationProb)) {   		
        		this.store.addKey(this);
        		(this.store.getLog(this)).append((Object) RandomHelper.nextIntFromTo(20, 1500));
        		newEvents++;
        	}
    	
        	if (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() % 10 == 0)
	        	if (this.active) {
	        		//might leave the network
	            	if (RandomHelper.nextDoubleFromTo(0.0, 1.0) > (1.0-this.leaveProb)) {
	            		this.active = false; 
	            		this.incomingWavefronts.clear();
	            		
	        			Network<Object> net = (Network<Object>) ContextUtils.getContext(this).getProjection("msgNet");
	        			
	        			net.getEdges(this).forEach(e-> {
	        				net.removeEdge(e);
	        			});
	        			
	            	}
	        	} else {
	        		//might join the network
	            	if (RandomHelper.nextDoubleFromTo(0.0, 1.0) > (1.0-this.spawnProb)) {
	            		this.active = true;
	            	}	
	        	}
    	}
    	
		List<Wavefront> remove = new ArrayList<Wavefront>();
		incomingWavefronts.forEach((w -> {
			Object rcv = w.live();
			
			if (rcv != null) {
	        	if (RandomHelper.nextDoubleFromTo(0.0, 1.0) > (this.lossProb)) {	
	        		
	        		if (w instanceof PushPullWavefront) {
	        			Network<Object> net = (Network<Object>) ContextUtils.getContext(this).getProjection("msgNet");
	        			
	        			net.getEdges(this).forEach(e-> {
	        				if (e.getSource() == this) {
	        					net.removeEdge(e);
	        				}
	        			});
	        			
	        			net.addEdge(w.getSource(), this);
	        			
	        			//Send back personal store.
	        			System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount() + " " + this + " received pushpull from " + w.getSource() + " " + w.msgBirth);
	        			this.aether.sendWavefront(new ReplyWavefront(
	        					this, w.getSource(),(Object) this.store, 
	        					RunEnvironment.getInstance().getCurrentSchedule().getTickCount(),
	        					this.neighbours.get(w.getSource())));
					} else {
						System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount() + " " + this + " received reply from " + w.getSource() + " " + w.msgBirth);
					}
	        		this.onSense(rcv);
	        	} 
	        	remove.add(w);
			}
		}));
		
		remove.forEach((w) -> {
			incomingWavefronts.remove(w);
		});

    }
    
    @ScheduledMethod(start = 300, interval = 300)
    public  void sync() {
    	if (this.getActive()) {
    		Relay r = this.aether.getRandomRelay(this);
    		
    		this.aether.sendWavefront(new PushPullWavefront(
    			this, r,(Object) this.store, 
    			RunEnvironment.getInstance().getCurrentSchedule().getTickCount(),
    			this.neighbours.get(r)));
    	}
    }
    
    public abstract void onSense(Object p);    
    public abstract boolean compareStatus(Relay r);
    public abstract void printStatus();
    
    //Analysis
    public int getNewEvents() {
    	int total = this.newEvents;
    	this.newEvents = 0;
    	return total;
    }
}
