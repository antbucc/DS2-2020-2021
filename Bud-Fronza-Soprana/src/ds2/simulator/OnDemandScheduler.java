package ds2.simulator;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.utility.Event;
import ds2.utility.Timestamped;

/**
 * This class represents a scheduler that creates events on demand (when update is called)
 * This allows this type of scheduler to memorize just one event (the next one to serve).
 * This is usually used to create an infinite quantity of event by creating them at regular intervals
 *
 * @param <T> The type of event to create/return
 */
public abstract class OnDemandScheduler<T extends Event> implements Scheduler<T> {
	public @Nullable Timestamped<T> ev;
		
	@Override
	public @Nullable Timestamped<T> peek() {
		return this.ev;
	}
	
	@Override
	public @Nullable Timestamped<T> poll() {
		Timestamped<T> polled = this.peek();
		this.ev = null;
				
		return polled;
	}
}
