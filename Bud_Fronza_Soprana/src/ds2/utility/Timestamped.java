package ds2.utility;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

public class Timestamped<T extends Event> implements Comparable<Timestamped<T>> {
	private double timestamp;
	private @NonNull T data;

	public double getTimestamp() {
		return timestamp;
	}
	
	public @NonNull T getData() {
		return data;
	}
	
	public String csv() {
		return timestamp + ", " + data.getClass().getName() + ", " + data.csv();
	}
	
	@Override
	public String toString() {
		return this.csv();
	}
	
	@Override
	public int compareTo(Timestamped<T> arg) {
		return Double.compare(this.timestamp, arg.timestamp);
	}
	
	public Timestamped(double timestamp, @NonNull T data) {
		this.timestamp = timestamp;
		this.data = data;
	}
}
