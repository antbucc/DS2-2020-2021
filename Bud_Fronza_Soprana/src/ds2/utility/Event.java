package ds2.utility;

// Support libraries
import org.jetbrains.annotations.NotNull;

// Custom libraries
import ds2.nodes.Address;

public abstract class Event {
	@NotNull Address target;

	public @NotNull Address getDestination() {
		return target;
	}
	
	public abstract String csv();
	
	public Event(@NotNull Address addr) {
		this.target = addr;
	}
}
