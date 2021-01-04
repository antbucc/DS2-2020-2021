package ds2.simulator;

// Support libraries
import org.jetbrains.annotations.NotNull;

// Custom libraries
import ds2.nodes.Address;

/**
 * This represents an Event. An event can be handled at different levels (by Oracle, Machine, Protocol, Application)
 * The event doesn't do any actual computation but is just a message/trigger between the various layers
 */
public abstract class Event {	
	@NotNull Address target;

	/**
	 * Constructor for Event
	 * @param addr The targeted node
	 */
	public Event(@NotNull Address addr) {
		this.target = addr;
	}
	
	/**
	 * Returns the destination/target of the event
	 * @return the address of the destination/target
	 */
	public @NotNull Address getDestination() {
		return target;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}
