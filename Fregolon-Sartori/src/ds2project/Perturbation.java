package ds2project;

import repast.simphony.engine.environment.RunEnvironment;

public class Perturbation {
	protected Relay1 src;
	protected Integer ref;
	protected String val;
	protected double generationTick;
	
	public Perturbation(Relay1 src, Integer ref, String val) {
		this.src = src;
		this.ref = ref;
		this.val = val;
		generationTick=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}

	public Relay1 getSrc() {
		return src;
	}

	public Integer getRef() {
		return ref;
	}

	public String getVal() {
		return val;
	}
	
	

}
