package ds2.application;

// Standard libraries
import java.security.PublicKey;

// Custom libraries
import ds2.nodes.Address;

/**
 * This class is used to contain the data which should be considered meant for the application to consume as data coming from the network
 *
 * @param <T> The data to be handled
 */
public class ApplicationData<T> extends ApplicationEvent {
	T data;

	/**
	 * Returns the data contained
	 * @return the data contained/to be handled
	 */
	public T getData() {
		return data;
	}
	
	@Override
	public String toString() {
		return this.getData().getClass().getName() + " " + this.getData().toString();
	}
	
	/**
	 * Constructor of ApplicationData
	 * @param destination The address that will receive this event
	 * @param port The port that will receive this event
	 * @param data The data to be handled by the application
	 */
	public ApplicationData(Address destination, PublicKey port, T data) {
		super(destination, port);
		this.data = data;
	}
}
