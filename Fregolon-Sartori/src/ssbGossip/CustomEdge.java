package ssbGossip;

import java.util.LinkedList;

import interfaces.Node;
import interfaces.Packet;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.graph.RepastEdge;
import ssbGossip.Helpers.ContextHelper;
import ssbGossip.Helpers.ParamHelper;

public class CustomEdge<T> extends RepastEdge<T> {
	enum LinkState {
		EMPTY, ACTIVE, FULL
	}

	private double bandwidth;
	private LinkState state = LinkState.EMPTY;
	private LinkedList<RelayedPacket> inTransit = new LinkedList<>();
	private LinkedList<RelayedPacket> waitQueue = new LinkedList<>();
	private int end = 0;
	private boolean src = false;
	private boolean trg = false;
	private final double CONVERSION_CONSTANT = 0.128; //from Kbps

	public CustomEdge(T arg0, T arg1, boolean arg2, double arg3) {
		super(arg0, arg1, arg2, arg3);
		this.bandwidth = ParamHelper.bandwidth*CONVERSION_CONSTANT;
		
	}

	private void updateState() {
		if (inTransit.isEmpty())
			this.state = LinkState.EMPTY;
		else
			this.state = LinkState.FULL;
	}

	public void doAction() {
		// if there are perturbations waiting in the queue
		if (!waitQueue.isEmpty()) {
			// take the first in queue and move it to "inTransit"
			RelayedPacket p = waitQueue.pop();
			inTransit.add(p);
			updateState();
			/*
			 * schedule the delivery of the packet based on the weight of the edge, which
			 * corresponds to the distance multiplied by the propagation speed
			 */
			//Propagation delay
			RunEnvironment.getInstance().getCurrentSchedule().schedule(
					ScheduleParameters.createOneTime(
							RunEnvironment.getInstance().getCurrentSchedule().getTickCount() + getWeight()),
					this, "deliver", p);
		}
		if (!waitQueue.isEmpty()) {
			ISchedule s = RunEnvironment.getInstance().getCurrentSchedule();
			//Transmission delay
			s.schedule(ScheduleParameters.createOneTime(s.getTickCount() + waitQueue.peek().getPacket().getSize()/bandwidth), this, "doAction");
		}
	}

	public void take(Packet p, Node n) {
		if (p instanceof FrontierMessage) {
			if (this.getSource() == n) {
				src = true;
			} else {
				trg = true;
			}
		}
		Node dest = (Node) (this.source == n ? this.target : this.source);
		RelayedPacket rp = new RelayedPacket(p, dest);

		if (waitQueue.isEmpty()) {
			ISchedule s = RunEnvironment.getInstance().getCurrentSchedule();
			s.schedule(ScheduleParameters.createOneTime(s.getTickCount() + p.getSize()/bandwidth), this, "doAction");
		}
		waitQueue.add(rp);
		updateState();

	}

	public LinkState getState() {
		return state;
	}

	public void deliver(RelayedPacket rp) {
		if (inTransit.remove(rp)) {
			Node dest = rp.getNode();
			dest.onReceive(rp.getPacket(), (CustomEdge<Node>) this);
		}
		updateState();
	}

	public void end() {
		// if both the nodes receive an end by the other the connection is closed
		if (++end == 2)
			ContextHelper.graph.removeEdge((RepastEdge) this);
	}

	// tells the node if it already asked for news
	public boolean asked(Node n) {
		return this.getSource() == n ? src : trg;
	}
}
