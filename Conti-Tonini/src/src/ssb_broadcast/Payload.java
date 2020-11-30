package ssb_broadcast;

/**
 * Defines a message to be sent inside
 * a Perturbation
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class Payload implements Comparable {
	// Type of the Payload
	public enum PayloadType {
		BROADCAST,
		ARQ_REQUEST,
		ARQ_REPLY,
		POINT_TO_POINT
	}
	
	// ID of the Observer who created this Payload
	private final int source;
	
	// Observer destination ID of this Payload. Value is check only if PayloadType has value POINT_TO_POINT
	private final int destination;
	
	// Reference value of this Payload
	private final int ref;
	
	// Value of this Payload. The actual content of the message
	private final int val;
	private final PayloadType type;
	
	public Payload(int source, int ref, int val, PayloadType type) {
		this.source = source;
		this.ref = ref;
		this.val = val;
		this.type = type;
		this.destination = -1;
	}
	
	public Payload(int source, int ref, int val, int destination) {
		this.source = source;
		this.ref = ref;
		this.val = val;
		this.type = PayloadType.POINT_TO_POINT;
		this.destination = destination;
	}
	
	public int getSource() {
		return this.source;
	}
	
	public int getDestination() {
		return this.destination;
	}
	
	public int getRef() {
		return this.ref;
	}
	
	public int getVal() {
		return this.val;
	}
	
	public PayloadType getType() {
		return this.type;
	}

	@Override
	public int compareTo(Object o) {
		return this.getVal() - ((Payload)o).getVal();
	}
	
	@Override
	public String toString() {
		return "<" + this.getSource() + "," + this.getRef() + "," + this.getVal() + ">";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Payload)) {
			return false;
		}
		
		Payload externalPayload = (Payload)o;

		return externalPayload.getSource() == this.getSource() && externalPayload.getRef() == this.getRef();
	}
}
