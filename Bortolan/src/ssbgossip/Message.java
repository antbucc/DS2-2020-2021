package ssbgossip;

import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;

public class Message {
	public enum MsgType {
		A, B, C, D
	}

	private final MsgType type;
	private final Participant src;
	private final Participant dst;
	private final Map<String, Object> content;
	private final RepastEdge<Object> edge;
	private final int distance;
	private final int deliveryTime;

	public Message(MsgType type, Participant src, Participant dst, Map<String, Object> content,
			RepastEdge<Object> edge) {
		this.type = type;
		this.src = src;
		this.dst = dst;
		this.content = content;
		this.edge = edge;
		this.distance = getDistance(src, dst);
		this.deliveryTime = (int) getTick() + distance/2;
	}

	public MsgType getType() {
		return type;
	}

	public Participant getSrc() {
		return src;
	}

	public Participant getDst() {
		return dst;
	}

	public Map<String, Object> getContent() {
		return content;
	}

	public RepastEdge<Object> getEdge() {
		return edge;
	}

	public int getDeliveryTime() {
		return deliveryTime;
	}

	private double getTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}

	private double getDelayCoefficient() {
		return (double) RunEnvironment.getInstance().getParameters().getValue("delay_coefficient");
	}

	private int getDistance(Participant src, Participant dst) {
		Context<Object> context = (Context<Object>) ContextUtils.getContext(src);
		ContinuousSpace<Object> space = (ContinuousSpace<Object>) context.getProjection("space");
		NdPoint pt1 = space.getLocation(src);
		NdPoint pt2 = space.getLocation(dst);
		return (int) space.getDistance(pt1, pt2);
	}
}
