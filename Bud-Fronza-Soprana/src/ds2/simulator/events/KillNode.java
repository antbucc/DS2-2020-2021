package ds2.simulator.events;

// Custom libraries
import ds2.nodes.Address;
import ds2.simulator.SimulationEvent;

/**
 * This event triggers the destruction of a node
 */
public class KillNode extends SimulationEvent {
	/**
	 * Constructor for KillNode
	 * @param addr The address of the node to kill
	 */
	public KillNode(Address addr) {
		super(addr);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
