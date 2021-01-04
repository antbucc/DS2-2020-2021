package theL0nePr0grammer_2ndAssignment.Wavefronts;

import repast.simphony.engine.environment.RunEnvironment;
import theL0nePr0grammer_2ndAssignment.Relays.Relay;

public abstract class Wavefront {
	private Relay source; //The source of the Wavefront is different from the source of the Perturbation (as perturbation are usually forwarded)
	private Relay dest;
	private Object msg;
	
	public double msgBirth;
	private double msgLife;
	
	public Wavefront(Relay source, Relay dest, Object msg, double msgBirth, double distance) {
		this.source = source;
		this.dest = dest;
		this.msg = msg;
		
		this.msgBirth = msgBirth;
		this.msgLife = source.getMessageLife()*distance;
	}
	
	public Relay getSource() {
		return this.source;
	}
	
	public Relay getDest() {
		return this.dest;
	}
	
	public Object getMsg() {
		return this.msg;
	}
	
	public Object live() {
		if (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() > this.msgBirth+this.msgLife) {
			return this.msg;
		}
		return null;
	}
}
