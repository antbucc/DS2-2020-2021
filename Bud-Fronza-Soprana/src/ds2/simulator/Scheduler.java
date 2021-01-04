package ds2.simulator;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.utility.Event;
import ds2.utility.Timestamped;

/**
 * A scheduler represents a "list" of events which will happen
 *
 * @param <T> The type event this scheduler returns
 */
public interface Scheduler<T extends Event> {
	/**
	 * Returns the first event (the earliest in time) without removing it from the "list"
	 * @return A timestamped event. it can be null if no event is in the "list"
	 */
	public @Nullable Timestamped<T> peek();	
	
	/**
	 * Returns and removes the first event (the earliest in time)
	 * @return A timestamped event, can be null if no event is in the "list"
	 */
	public @Nullable Timestamped<T> poll();	
	
	/**
	 * This function can be overwritten to trigger an update of the scheduler at each step of the simulation.
	 * This "update" can be anything that is needed to execute just before checking for new events that the schedueler need to operate correctly
	 * @param oracle The oracle on which this scheduler is running
	 */
	public void update(Oracle oracle);
}
