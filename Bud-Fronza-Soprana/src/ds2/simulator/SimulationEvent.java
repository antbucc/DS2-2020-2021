package ds2.simulator;

// Custom libraries
import ds2.nodes.Address;
import ds2.utility.Event;

/**
 * This class represents an Event which should be handled in Oracle and pertains to some simulation property like the number of alive or dead nodes, ...
 */
public abstract class SimulationEvent extends Event {
	/**
	 * Constructor for SimulationEvent
	 * @param addr The address of the node used in this event
	 */
	public SimulationEvent(Address addr) {
		super(addr);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
