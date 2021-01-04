package ds2.nodes;

// Standard libraries
import java.security.PublicKey;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.utility.EventWithPort;

/**
 * This class represents all the events that are local to the node like timers
 */
public abstract class LocalEvent extends EventWithPort {
	/**
	 * Constructor for LocalEvent
	 * @param addr The address of the protocol which will receive this event
	 * @param port The port of the protocol which will receive this event
	 */
	public LocalEvent(@NonNull Address addr, PublicKey port) {
		super(addr, port);
	}
}
