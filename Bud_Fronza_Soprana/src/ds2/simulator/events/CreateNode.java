package ds2.simulator.events;

// Custom libraries
import ds2.nodes.Address;
import ds2.simulator.SimulationEvent;

public class CreateNode extends SimulationEvent {
	public CreateNode(Address addr) {
		super(addr);
	}
	
	@Override
	public String toString() {
		return "CreateNode[" + this.getDestination() + "]";
	}

	@Override
	public String csv() {
		return "" + this.getDestination();
	}
}
