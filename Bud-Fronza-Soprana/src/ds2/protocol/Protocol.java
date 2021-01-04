package ds2.protocol;

// Standard libraries
import java.security.PublicKey;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

import ds2.nodes.Address;
// Custom libraries
import ds2.nodes.LocalEvent;
import ds2.simulator.Oracle;
import ds2.utility.logging.Logger;
import ds2.utility.logging.MachineEventHandler;

// TODO: Maybe change to TCPProtocol or something like that (so that having a port makes sense)
/**
 * This class represent a protocol. This class must be extended with the real implementation of the protocol
 *
 * @param <UP> The type of the layer which sits on top of this layer
 * @param <DOWN> The type of the layer on which this layer sits on top
 */
public abstract class Protocol<UP extends MachineEventHandler<?, ?>, DOWN extends MachineEventHandler<?, ?>> extends MachineEventHandler<UP, DOWN> {

	private PublicKey port;
	
	/**
	 * Returns the port used by the protocol
	 * @return the port used by the protocol
	 */
	public PublicKey getPort() {
		return this.port;
	}

	/**
	 * This function schedules a specified local event for this protocol at a specified timestamp
	 * @param timestamp the timestamp at which to schedule the event
	 * @param ev the event to schedule
	 */
	public void scheduleLocalEvent(double timestamp, LocalEvent ev) {
		Oracle.getInstance().getLocalEventsScheduler().schedule(timestamp, ev);
	}

	/**
	 * This functions unicasts a specified message to a specified destination (automatically using the correct source address and port)
	 * @param <T> The type of the data contained in the message
	 * @param destination The address of the destination
	 * @param destPort The port of the destination
	 * @param data The data to send
	 */
	public <T> void unicast(Address destination, PublicKey destPort, T data) {
		this.getMachine().unicast(this.getPort(), destination, destPort, data);
	}
	
	/**
	 * Constructor for Protocol
	 * @param down The layer under the protocol (usually a machine)
	 * @param port The port used by the protocol
	 */
	public Protocol(DOWN down, PublicKey port) {
		super(down);
		this.port = port;
		
		log("Protocol", "Created with port "+ Logger.formatPort(this.getPort()));
	}
	
	// -- Logging
	/**
	 * Log something for this protoco.l using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, @NonNull String msg) {
		this.log(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}
	
	/**
	 * Log something for this protocol using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, double timestamp, @NonNull String msg) {
		Oracle.getInstance().getLogger().getMachineLogger(this.getAddress()).print(System.out, msg, tag, ""+timestamp, this.getAddress().toString(), Logger.formatPort(this.getPort()));
	}

	/**
	 * Log an error for this protocol using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void err(@NonNull String tag, @NonNull String msg) {
		this.err(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}

	/**
	 * Log an error for this protocol using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void err(@NonNull String tag, double timestamp, @NonNull String msg) {
		Oracle.getInstance().getLogger().getMachineLogger(this.getAddress()).print(System.err, msg, tag, ""+timestamp, this.getAddress().toString(), Logger.formatPort(this.getPort()));
	}
}
