package ssb_broadcast;

/**
 * Defines a message to be sent inside
 * a Perturbation
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class Payload {
	// Type of the Payload
	public enum PayloadType {
		DISCOVERY,
		REQUEST,
		REPLY
	}
	
	// Public key of the Observer who created this Payload
	private final String source;
	
	// Observer destination public key of this Payload
	private final String destination;
	
	// Reference value of this Payload
	private final int ref;
	
	// Value of this Payload. The actual content of the message
	private final Store val;
	
	// Type of this payload
	private final PayloadType type;

	public Payload(String source, String destination, Store val, int ref, PayloadType type) {
		this.source = source;
		this.ref = ref;
		this.val = val;
		this.destination = destination;
		this.type = type;
	}
	
	public String getSource() {
		return this.source;
	}
	
	public String getDestination() {
		return this.destination;
	}
	
	public int getRef() {
		return this.ref;
	}
	
	public Store getVal() {
		return this.val;
	}
	
	public PayloadType getType() {
		return this.type;
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