package ssb_broadcast;

import java.util.Arrays;

import ssb_broadcast.utils.CryptoUtil;

/**
 * Defines an Event of this simulation
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class Event {
	// Defines the type of an Event
	public enum EventType {
		MESSAGE,
		FOLLOW,
		UNFOLLOW,
		BLOCK,
		UNBLOCK
	}
	
	// Public key
	private final String id;
	
	// Hash of the previous event in the log. Null if first
	private final int previous;
	
	// Sequence number
	private final int index;
	
	// Event's content
	private final String content;
	
	// Signature of id, previous, index and content
	private byte[] signature;
	
	// Type of the event
	private final EventType type;
	
	public Event(EventType type, String id, int index, String content, int previous) {
		super();
		this.id = id;
		this.previous = previous;
		this.index = index;
		this.content = content;
		this.type = type;
	}

	public EventType getType() {
		return this.type;
	}
	
	public String getId() {
		return id;
	}

	public int getPrevious() {
		return previous;
	}

	public int getIndex() {
		return index;
	}

	public String getContent() {
		return content;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void sign(String privateKey) {
		this.signature = CryptoUtil.sign(privateKey, this.toString().getBytes());
	}
	
	public boolean verify(String publicKey) {
		return CryptoUtil.verify(publicKey, this.toString().getBytes(), this.signature);
	}
	
	public Event clone() {
		Event cloneEvent = new Event(this.type, this.id, this.index, this.content, this.previous);
		cloneEvent.signature = this.signature.clone();
		
		return cloneEvent;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + index;
		result = prime * result + previous;
		result = prime * result + Arrays.hashCode(signature);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Event other = (Event) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!content.equals(other.content)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (index != other.index) {
			return false;
		}
		if (previous != other.previous) {
			return false;
		}
		if (!Arrays.equals(signature, other.signature)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return "Event [id=" + id + ", previous=" + previous + ", index=" + index + ", content=" + content + ", type="
				+ type + "]";
	}
}