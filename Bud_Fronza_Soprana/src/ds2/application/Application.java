package ds2.application;

import ds2.nodes.Address;
// Custom libraries
import ds2.protocol.Protocol;

/**
 * This class represent the application on top of the protocol and must be extended into a real application in order to use it
 * 
 * @param <T> The type of the protocol onto which the application sits
 */
public abstract class Application<T extends Protocol<?>> {
	protected T protocol = null;
	
	/**
	 * Function to handle all ApplicationData. This call received the data going upwards from the {@link Protocol}
	 * @param appData The application data to handle
	 */
	public abstract void handle(ApplicationData appData);
	/**
	 * Function to handle all ApplicationEvents. This call received this events and might call the protocol as a reaction (e.g.: {@link GenerateBroadcastEvent} will cause this function to generate and broadcast the event)
	 * @param applicationEvent The event to handle
	 */
	public abstract void handleApplicationEvent(ApplicationEvent applicationEvent);
	
	/**
	 * This functions sets the protocol onto which the application sits
	 * @param protocol The protocol to use
	 */
	public void setProtocol(T protocol) {
		this.protocol = protocol;
	}
	
	/**
	 * Returns the address of the machine onto which this application sits
	 * @return the address of the machine running this application
	 */
	public Address getAddress() {
		return this.protocol.getAddress();
	}
	
	/**
	 * Default constructor for the application
	 */
	public Application() {}
}
