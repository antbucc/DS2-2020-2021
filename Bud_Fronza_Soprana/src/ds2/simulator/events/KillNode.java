package ds2.simulator.events;

// Custom libraries
import ds2.nodes.Address;
import ds2.simulator.SimulationEvent;

public class KillNode extends SimulationEvent {
	public KillNode(Address addr) {
		super(addr);
	}

	@Override
	public String toString() {
		return "KillNode[" + this.getDestination() + "]";
	}

	@Override
	public String csv() {
		return "" + this.getDestination();
	}
}
