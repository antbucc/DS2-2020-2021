package ds2.simulator;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.utility.Event;
import ds2.utility.Timestamped;

public interface Scheduler<T extends Event> {
	public @Nullable Timestamped<T> peek();	
	public @Nullable Timestamped<T> poll();	
	public void update(Oracle oracle);
}
