package ds2.network;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.nodes.Address;
import ds2.protocol.messages.Perturbation;
import ds2.utility.Event;

/**
 * An {@link Event} representing the reception of a message
 */
public class NetworkMessage extends Event {
	@NonNull Address source;
	@Nullable Perturbation data;
	
	boolean received;

	/**
	 * Get the data received
	 * @return the date received
	 */
	public @Nullable Perturbation getData() {
		return data;
	}
	
	/**
	 * Specify if the message should be received or not
	 * @param received true if the message should be received, false otherwise
	 */
	public void setReceived(boolean received) {
		this.received = received;
	}
	
	/**
	 * Check the reception state of message
	 * @return true it it will be received, false otherwise
	 */
	public boolean isReceived() {
		return received;
	}
	
	public Address getSource() {
		return source;
	}
	
	/**
	 * Constructor for NeworkMessage
	 * @param destination The address of the node receiving the message
	 * @param received true if the node should receive the message, false otherwise
	 * @param data The data to be received
	 */
	public NetworkMessage(
			@NonNull Address source, 
			@NonNull Address destination, 
			boolean received,
			@Nullable Perturbation data) {
		super(destination);
		this.source = source;
		this.received = received;
		this.data = data;
	}

	@Override
	public String csv() {
		return "sentBy=" + this.getSource() + ", sendingTo=" + this.getDestination() + ", P=<" + this.data.toString()+">";
	}
}
