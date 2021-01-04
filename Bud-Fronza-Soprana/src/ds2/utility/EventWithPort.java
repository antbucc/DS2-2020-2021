package ds2.utility;

// Standard libraries
import java.security.PublicKey;

// Custom libraries
import ds2.nodes.Address;
import ds2.utility.logging.Logger;

/**
 * An event which addresses a specific Protocol (as it has both the address and port)
 */
public class EventWithPort extends Event {
	PublicKey port;

	/**
	 * Constructor of EventWithPort
	 * @param addr The address of the protocol that should receive this event
	 * @param port The port of the protocol that should receive this event
	 */
	public EventWithPort(Address addr, PublicKey port) {
		super(addr);	
		this.port = port;
	}
	
	/**
	 * Obtain the port of the event
	 * @return Return the port that should receive this event
	 */
	public PublicKey getPort() {
		return port;
	}

	@Override
	public String toString() {
		return this.getDestination() + " " + Logger.formatPort(this.getPort());
	}
}
