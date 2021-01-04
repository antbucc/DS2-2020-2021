package ds2.network;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;

// Standard libraries
import java.security.PublicKey;

// Custom libraries
import ds2.nodes.Address;
import ds2.utility.EventWithPort;
import ds2.utility.logging.Logger;

/**
 * An {@link EventWithPort} representing the reception of a message
 *
 * @param <T> The type of the data contained
 */
public class NetworkMessage<T> extends EventWithPort {
	@NonNull Address source;
	@NonNull PublicKey sourcePort;
	
	@Nullable T data;
	
	boolean received;

	/**
	 * Constructor for NeworkMessage
	 * @param source The address of the protocol/node sending the message
	 * @param sourcePort The port of the protocol/node sending the message
	 * @param destination The address of the protocol receiving the message
	 * @param destPort The port of the protocol receiving the message
	 * @param received true if it should be received, false otherwise
	 * @param data The data to be received
	 */
	public NetworkMessage(
			@NonNull Address source, 
			@NonNull PublicKey sourcePort,
			@NonNull Address destination, 
			@NonNull PublicKey destPort,
			boolean received,
			@Nullable T data) {
		super(destination, destPort);
		this.source = source;
		this.sourcePort = sourcePort;
		this.received = received;
		this.data = data;
	}
	
	/**
	 * Get the data received
	 * @return the date received
	 */
	public @Nullable T getData() {
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
	

	/**
	 * Get the source of the message
	 * @return the address of the source of the message
	 */
	public Address getSource() {
		return source;
	}

	/**
	 * Get the source port of the message
	 * @return the port, i.e. PublicKey that identifies the identity/protocol instance.
	 */
	public PublicKey getSourcePort() {
		return sourcePort;
	}
	
	
	public String toString() {
		return super.toString() + " senderAddr=" + this.getSource() + ", senderPort=" + Logger.formatPort(this.getSourcePort()) + ", P=<" + this.data.toString()+">";
	}
}
