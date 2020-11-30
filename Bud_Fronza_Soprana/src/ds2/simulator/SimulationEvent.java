package ds2.simulator;

// Custom libraries
import ds2.nodes.Address;
import ds2.utility.Event;

public abstract class SimulationEvent extends Event {
	public SimulationEvent(Address addr) {
		super(addr);
	}
}
