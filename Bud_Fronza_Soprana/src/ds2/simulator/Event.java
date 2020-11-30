package ds2.simulator;

// Support libraries
import org.jetbrains.annotations.NotNull;

// Custom libraries
import ds2.nodes.Address;

public abstract class Event {	
	@NotNull Address target;

	public Event(@NotNull Address addr) {
		this.target = addr;
	}
	
	public @NotNull Address getDestination() {
		return target;
	}
	
	public abstract String csv();
}
