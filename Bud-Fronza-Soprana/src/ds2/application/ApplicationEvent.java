package ds2.application;

// Standard libraries
import java.security.PublicKey;

// Custom libraries
import ds2.nodes.Address;
import ds2.utility.EventWithPort;

/**
 * This class represents an event generated and handled inside the application
 */
public abstract class ApplicationEvent extends EventWithPort {

	/**
	 * Constructor for an ApplicationEvent
	 * @param addr The address of the application which will receive this event
	 * @param port The port of the application which will receive this event
	 */
	public ApplicationEvent(Address addr, PublicKey port) {
		super(addr, port);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
