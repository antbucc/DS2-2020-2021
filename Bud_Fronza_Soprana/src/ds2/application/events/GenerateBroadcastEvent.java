package ds2.application.events;

// Custom libraries
import ds2.application.ApplicationEvent;
import ds2.nodes.Address;

/**
 * This class is an {@link ApplicationEvent} used to signal to the {@link Application} that it should generate an event and broadcast it
 */
public class GenerateBroadcastEvent extends ApplicationEvent {
	/**
	 * Constructor for the event
	 * @param addr The address of the machine that will receive the event
	 */
	public GenerateBroadcastEvent(Address addr) {
		super(addr);
	}
}
