package distributedgroup_project2.application;

import distributedgroup_project2.CryptoUtils;
import distributedgroup_project2.PrivateKey;
import distributedgroup_project2.PublicKey;

public class Event {
	enum EventType {
		MESSAGE,
		FOLLOW,
		UNFOLLOW,
		BLOCK,
		UNBLOCK,
	}
	
	private final PublicKey creatorId;
	private final Integer previous;
	private final int index;
	private final Object content;
	private final EventType type;
	private final String signature;
	
	/**
	 * Constructor of the class Event.
	 * 
	 * @param key: the private key of the creator of the event;
	 * @param creatorId: the public key of the creator of the event;
	 * @param previous: the hash of the previous event;
	 * @param index: the index of the event in the log;
	 * @param type: the type of the event;
	 * @param content: the content of the event;
	 */
	public Event(PrivateKey key, PublicKey creatorId,
			Integer previous, int index, EventType type, Object content) {
		this.creatorId = creatorId;
		this.previous = previous;
		this.index = index;
		this.type = type;
		this.content = content;
		
		this.signature = sign(key);
	}

	/**
	 * Method to retrieve the hash of the previous event.
	 * 
	 * @return: the hash of the previous event.
	 */
	public Integer getPrevious() {
		return previous;
	}

	/**
	 * Method to retrieve the signature of the event.
	 * 
	 * @return: the signature of the event.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Method to retrieve the public key of the creator of the event.
	 * 
	 * @return: the public key of the creator of the event.
	 */
	public PublicKey getCreatorId() {
		return creatorId;
	}

	/**
	 * Method to retrieve the index of the event.
	 * 
	 * @return: the index of the event.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Method to retrieve the content of the event.
	 * 
	 * @return: the content of the event.
	 */
	public Object getContent() {
		return content;
	}

	/**
	 * Method to retrieve the type of the event.
	 * 
	 * @return: the type of the event.
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * Method to serialize the event.
	 * 
	 * @return: the String representation of the event.
	 */
	public String serialize() {
		return this.creatorId.toString() + 
			   (this.previous == null ? "" : this.previous.toString()) + 
			   String.valueOf(this.index) +
			   this.content.toString();
	}

	/**
	 * Method to sign the event using the creator private key.
	 * 
	 * @return: the signature of the event.
	 */
	private String sign(PrivateKey key) {
		String payload = this.serialize();
		
		try {
			return CryptoUtils.sign(key, payload);
		} catch (Exception e) {
			System.err.println("Cannot sign event " + String.valueOf(index));
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method to check the validity of the signature of the event.
	 * 
	 * @return: true if the signature is valid, false otherwise.
	 */
	public boolean verifySignature() {
		return CryptoUtils.verify(this.creatorId, this.signature, this.serialize());
	}
	
}
