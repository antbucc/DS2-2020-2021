package ds2.application.events;

// Standard libraries
import java.security.PublicKey;

// Custom libraries
import ds2.application.ApplicationEvent;
import ds2.nodes.Address;

/**
 * This class is an {@link ApplicationEvent} used to signal to the application that it should block another node
 */
public class BlockEvent extends ApplicationEvent {
	/**
	 * Constructor for BlockEvent
	 * @param addr The address of the protocol that will receive the event
	 * @param port The port of the protocol that will receive the event
	 */
	public BlockEvent(Address addr, PublicKey port) {
		super(addr, port);
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
