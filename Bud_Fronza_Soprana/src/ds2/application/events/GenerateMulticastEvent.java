package ds2.application.events;

// Custom libraries
import ds2.application.ApplicationEvent;
import ds2.nodes.Address;

/**
 * This class is an {@link ApplicationEvent} used to signal to the {@link Application} that it should generate an event and send it in multicast 
 * The topic is chosen at random by the application
 */
public class GenerateMulticastEvent extends ApplicationEvent {
	/**
	 * Constructor for the event
	 * @param addr Address of the machine which will receive the event
	 */
	public GenerateMulticastEvent(Address addr) {
		super(addr);
	}
}
