package ds2.nodes.events;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.nodes.Address;
import ds2.nodes.LocalEvent;

/**
 * This local event is generate and used by the node to trigger the ARQ at a specific time
 */
public class TriggerARQ extends LocalEvent {
	public TriggerARQ(@NonNull Address addr) {
		super(addr);
	}

	@Override
	public String csv() {
		return "triggerOn=" + this.getDestination().toString();
	}
}
