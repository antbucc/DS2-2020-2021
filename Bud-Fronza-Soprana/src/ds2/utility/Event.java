package ds2.utility;

// Support libraries
import org.jetbrains.annotations.NotNull;

// Custom libraries
import ds2.nodes.Address;

/**
 * This class represents an event (a message) that can be exchange between the varius layer to exchange information or trigger behaviors
 */
public abstract class Event {
	@NotNull Address target;

	/**
	 * Return the address of the node which is target by the Event
	 * @return The address of the targeted node
	 */
	public @NotNull Address getDestination() {
		return target;
	}
	
	/**
	 * Constructor for Event
	 * @param addr The address of the node targeted by the Event
	 */
	public Event(@NotNull Address addr) {
		this.target = addr;
	}
	
	@Override
	public String toString() {
		return "" + this.target;
	}
}
