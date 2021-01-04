package ds2.utility;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

/**
 * This class represents an event in time
 * @param <T> The type of event represented
 */
public class Timestamped<T extends Event> implements Comparable<Timestamped<T>> {
	private double timestamp;
	private @NonNull T data;

	/**
	 * Returns the (simulation) time at which the event will be processed
	 * @return the (simulation) time at which the event will be processed
	 */
	public double getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Returns the event contained 
	 * @return the event contained
	 */
	public @NonNull T getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return timestamp + ", " + data.getClass().getName() + ", " + data.toString();
	}
	
	@Override
	public int compareTo(Timestamped<T> arg) {
		int r = Double.compare(this.timestamp, arg.timestamp);
		if (r != 0)
			return r;
		
		return 1;
	}
	
	/**
	 * Constructor for timestamped
	 * @param timestamp The timestamp at which the event should be processed
	 * @param data The data to contain
	 */
	public Timestamped(double timestamp, @NonNull T data) {
		this.timestamp = timestamp;
		this.data = data;
	}
}
