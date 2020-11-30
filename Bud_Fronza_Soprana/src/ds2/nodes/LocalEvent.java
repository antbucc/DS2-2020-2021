package ds2.nodes;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.utility.Event;

/**
 * This class represents all the events that are local to the node like timers
 */
public abstract class LocalEvent extends Event {
	public LocalEvent(@NonNull Address addr) {
		super(addr);
	}
}
