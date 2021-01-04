package ds2.nodes.events;

// Standard libraries
import java.security.PublicKey;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.nodes.Address;
import ds2.nodes.LocalEvent;

/**
 * This local event is generate and used by the node to trigger the ssb update at a specific time
 */
public class TriggerUpdate extends LocalEvent {
	/**
	 * Constructor for the TriggerUpdate event
	 * @param addr The address to which we want to send this event
	 * @param port The port to which we want to send this event
	 */
	public TriggerUpdate(@NonNull Address addr, PublicKey port) {
		super(addr, port);
	}

	public String toString() {
		return super.toString();
	}
}
