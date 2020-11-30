package broadcastOnlyCommunication;

import java.util.Objects;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Perturbation {
	
	private final Grid<Object> grid;
	public final String senderId;
	public final int ref;
	public final String val;
	private float radius;
	public final String receiverId;
	private final String topic;
	private GridPoint center;
	
	/**
	 * number of ticks passed from its creation. Used for velocity and distance calculations
	 */
	private int ticks = 0;

	public Perturbation(Grid<Object> grid, String src, int ref, String val) {
		this.grid = grid;
		this.senderId = src;
		this.ref = ref;
		this.val = val;
		this.topic = null;
		this.receiverId = null;
	}
	
	public Perturbation(Grid<Object> grid, String senderId, int ref, String val, String receiverId, String topic) {
		this.grid = grid;
		this.senderId = senderId;
		this.ref = ref;
		this.val = val;
		this.receiverId = receiverId;
		this.topic = topic;
	}
	
	
	public Perturbation copy() {
		return new Perturbation(
				grid,
				senderId,
				ref,
				val,
				receiverId,
				topic
		);
	}
	
	
	public static Perturbation createUnicastPerturbation(Grid<Object> grid, String src, int ref, String val, String receiverId) {
		return new Perturbation(grid, src, ref, val, receiverId, null);
	}
	
	public static Perturbation createTopicPerturbation(Grid<Object> grid, String src, int ref, String val, String topic) {
		return new Perturbation(grid, src, ref, val, null, topic);
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void onTick() {
		increaseRadius();
		deliverToRelaysInsideReachingRing();
		ticks++;
		removeItselfWhenBiggerThanGrid();
	}

	private void deliverToRelaysInsideReachingRing() {
		// get the grid location of the Perturbation
		var pt = grid.getLocation(this);

		Utils
			.getAllRelaysInGrid(grid, this)
			.stream()
			.filter(relay -> {
				var dist = Utils.distanceBetweenPoints(pt, grid.getLocation(relay));
				var ringRadiusStart = radius - getRadiusIncrease();
				var ringRadiusEnd = radius;
				return dist >= ringRadiusStart && dist <= ringRadiusEnd;
			})
			.peek(relayInRange -> System.out.println("Deliverying perturbation to " + relayInRange))
			.forEach(relayInRange -> relayInRange.onSense(Perturbation.this));
	}

	private void increaseRadius() {
		if (ticks <= 0) {
			radius = 0.5f;
		} else {
			radius += getRadiusIncrease();
		}
	}
	
	private double getRadiusIncrease() {
		return 0.5 - sigmoid(ticks);
	}
	
	private static double sigmoid(double x) {
		return (1 / (1 + Math.pow(Math.E, -(x / 10)))) - 1;
	}
	
	private void removeItselfWhenBiggerThanGrid() {
		if (radius > Utils.getBiggestSizeOfGrid(grid)) {
			Utils.removePerturbation(this);
		}
	}
	
	/**
	 * Even though the radius is public, we need a getter method to use this field
	 * inside a Dataset
	 * @return
	 */
	public float getRadius() {
		return radius;
	}
	
	/**
	 * Even though the senderId is public, we need a getter method to use this field
	 * inside a Dataset
	 * @return
	 */
	public String getSenderId() {
		return senderId;
	}
	
	public boolean isUnicastMessage() {
		return receiverId != null;
	}
	
	public boolean isTopicMessage() {
		return topic != null;
	}
	
	public String getTopic() {
		if (!isTopicMessage()) {
			throw new IllegalStateException("Perturbation is not a Topic message");
		}
		return topic;
	}
	
	public void setCenter(GridPoint center) {
		this.center = center;
	}

	@Override
	public int hashCode() {
		return Objects.hash(receiverId, ref, senderId, topic, val);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Perturbation))
			return false;
		Perturbation other = (Perturbation) obj;
		return Objects.equals(receiverId, other.receiverId) && ref == other.ref
				&& Objects.equals(senderId, other.senderId) && Objects.equals(topic, other.topic)
				&& Objects.equals(val, other.val) && Objects.equals(center, other.center);
	}
	
	@Override
	public String toString() {
		return "Perturbation [senderId=" + senderId + ", ref=" + ref + ", val=" + val + "]";
	}
	
}
