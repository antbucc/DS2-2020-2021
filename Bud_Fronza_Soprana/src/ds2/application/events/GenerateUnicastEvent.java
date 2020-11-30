package ds2.application.events;

// Custom libraries
import ds2.application.ApplicationEvent;
import ds2.nodes.Address;

/**
 * This class is an {@link ApplicationEvent} used to signal to the {@link Application} that it should generate an event and unicast it
 * The destination of the unicast is chosen by the application
 */
public class GenerateUnicastEvent extends ApplicationEvent {
	/**
	 * Constructor of the event
	 * @param addr Address of the machine which will receive the event
	 */
	public GenerateUnicastEvent(Address addr) {
		super(addr);
	}
}
