package ds2.protocol.ssb;

import java.security.PublicKey;

import ds2.application.ApplicationData;

/**
 * Class which holds the information about an event in the Secure Scuttlebutt protocol.
 * 
 *
 */
public class SSBEvent {

	/**
	 * Public key of the creator
	 */
	private PublicKey idKey; 
	
	
	/**
	 * Hash of previous message, signed
	 */
	private String previous; 
	
	/**
	 * index of event in the log of the creator
	 */
	private int index; 
	
	/**
	 * Signature of id, previous, index and content with the Private Key of the creator.
	 */
	private String signature; 
	
	/**
	 * Application content that this event holds.
	 */
	private ApplicationData<?> content;
	
	/**
	 * Constructor. Initializes an SSBEvent with all the fields.
	 * @param id
	 * @param previous
	 * @param index
	 * @param signature
	 * @param content
	 */
	public SSBEvent(PublicKey id, String previous, int index, String signature, ApplicationData<?> content) {
		this.idKey = id;
		this.previous = previous;
		this.index = index;
		this.signature = signature; 
		this.content = content;
	}

	/**
	 * Getter of the id.
	 * @return PublicKey
	 */
	public PublicKey getIdKey() {
		return idKey;
	}

	/**
	 * Setter of the id.
	 * @param idKey
	 */
	public void setIdKey(PublicKey idKey) {
		this.idKey = idKey;
	}

	/**
	 * Gets the content of the 'previous' fields, it should match the signature of the 
	 * previous event in the log.
	 * @return a string representation of the content.
	 */
	public String getPrevious() {
		return previous;
	}

	/**
	 * Setter for the signature of the previous message.
	 * @param previous
	 */
	public void setPrevious(String previous) {
		this.previous = previous;
	}

	/**
	 * Gets the index of this event in the log.
	 * @return integer
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets the index of this event.
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Gets a string representation of the signature of this event.
	 * @return signature
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Sets the signarure of this event.
	 * @param signature
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * Gets the application data contained in the event.
	 * @return
	 */
	public ApplicationData<?> getContent() {
		return content;
	}

	/**
	 * Sets the application content.
	 * @param content
	 */
	public void setContent(ApplicationData<?> content) {
		this.content = content;
	}
	
	/**
	 * Returns a representation of the id, previous, index and content for hashing purposes.
	 * @return String, a concatenation of the various fields.
	 */
	public String getContents() {
		return 	this.idKey.toString() + " " + 
				this.previous + " " + 
				this.index + " " +
				this.content.toString();
	}
	
}
