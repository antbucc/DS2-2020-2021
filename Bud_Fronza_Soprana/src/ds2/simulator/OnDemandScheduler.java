package ds2.simulator;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.utility.Event;
import ds2.utility.Timestamped;

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
