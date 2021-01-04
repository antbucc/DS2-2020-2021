package ds2.simulator.events;

// Custom libraries
import ds2.nodes.Address;
import ds2.simulator.SimulationEvent;

/**
 * This event is used to instruct the simulation to kill a node and then revive it in the future
 */
public class GoOffline extends SimulationEvent {
	/**
	 * Constructor for GoOffline
	 * @param addr The address of the node which will go temporarily offline
	 */
	public GoOffline(Address addr) {
		super(addr);
	}

	public String toString() {
		return super.toString();
	}
}
