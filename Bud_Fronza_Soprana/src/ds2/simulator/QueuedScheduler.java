package ds2.simulator;

// Standard libraries
import java.util.TreeSet;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.utility.Event;
import ds2.utility.Timestamped;

public abstract class QueuedScheduler<T extends Event> implements Scheduler<T> {
	protected TreeSet<Timestamped<T>> queue = new TreeSet<>();

	@Override
	public @Nullable Timestamped<T> peek() {
		if (this.queue.isEmpty())
			return null;

		return this.queue.first();			
	}

	@Override
	public @Nullable Timestamped<T> poll() {
		return this.queue.pollFirst();
	}

	public void schedule(double timestamp, @NonNull T toSchedule) {
		this.queue.add(new Timestamped<>(timestamp, toSchedule));
	}
	
	@Override
	public void update(Oracle oracle) {	/* We don't need to do anything on update */ }	
}
