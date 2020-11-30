package ds2project;

import java.util.LinkedList;
import java.util.Random;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.RepastEdge;


public class CustomEdge<T> extends RepastEdge<T> {
	private int state = 0;
	private int maxP;
	private int maxQueue;
	private LinkedList<RelayedPertubation> inTransit = new LinkedList<>();
	private LinkedList<RelayedPertubation> waitQueue = new LinkedList<>();
	private float dropProb;
	
	public CustomEdge(T arg0, T arg1, boolean arg2, double arg3) {
		super(arg0, arg1, arg2, arg3);
		Parameters params = RunEnvironment.getInstance().getParameters();
		dropProb = params.getFloat("dropProb");
		maxP = params.getInteger("maxP");
		maxQueue = params.getInteger("maxQueue");
		RunEnvironment.getInstance().getCurrentSchedule().schedule(ScheduleParameters.createRepeating(
				RunEnvironment.getInstance().getCurrentSchedule().getTickCount()+1, 1), this,"doAction");
	}

	public int getState() {
		return state;
	}

	public void doAction() {
		//if there are perturbations waiting in the queue and I can carry another one
		if (!waitQueue.isEmpty() && inTransit.size() < maxP) {
			//take the first in queue and move it to "inTransit"
			RelayedPertubation p = waitQueue.pop();
			inTransit.add(p);
			updateState();
			/*schedule the delivery of the packet based on the weight of the edge,
			 * which corresponds to the distance multiplied by the propagation speed
			 */
			RunEnvironment.getInstance().getCurrentSchedule().schedule(
					ScheduleParameters.createOneTime(
							RunEnvironment.getInstance().getCurrentSchedule().getTickCount() + getWeight()),
					this, "deliver", p);
		}
	}
	
	public void deliver(RelayedPertubation p) {
		// remove the perturbation from the transit queue
		inTransit.remove(p);
		// check if i need to change the state of the link
		updateState();
		Random r = new Random();
		if (r.nextFloat() >= dropProb) {
			// check the node to which the perturbation needs to be delivered
			T n = p.getN() == this.getSource() ? this.getTarget() : this.getSource();
			Relay1 node = (Relay1) n;
			// deliver the perturbation
			node.onSense(p.getP());
		} else {
			p.getN().discarded();
		}
	}

	public void take(Perturbation p, Relay1 relay) {
		if(waitQueue.size()<maxQueue) {
			waitQueue.add(new RelayedPertubation(p, relay));
		} else {
			relay.discarded();
		}
	}
	
	private void updateState() {
		if (inTransit.isEmpty())
			this.state = 0;
		else if (inTransit.size() < maxP)
			this.state = 1;
		else
			this.state = 2;
	}
}
