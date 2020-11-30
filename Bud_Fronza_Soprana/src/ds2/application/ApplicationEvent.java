package ds2.application;

// Custom libraries
import ds2.nodes.Address;
import ds2.utility.Event;

/**
 * This class represents an event generated and handled inside the application
 */
public abstract class ApplicationEvent extends Event {

	/**
	 * Constructor for an ApplicationEvent
	 * @param addr The address of the machine which will receive this event
	 */
	public ApplicationEvent(Address addr) {
		super(addr);
	}

	@Override
	public String csv() {
		return "";
	}
}
