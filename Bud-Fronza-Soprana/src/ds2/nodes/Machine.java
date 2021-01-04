package ds2.nodes;

import java.security.PublicKey;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.protocol.Protocol;
import ds2.simulator.Oracle;
import ds2.utility.Event;
import ds2.utility.EventWithPort;
import ds2.utility.logging.MachineEventHandler;

/**
 * This class represents a node of the network and especially its physical layer
 *
 * @param <UP> The type of protocol which sits of top of the machine
 * @param <DOWN> Should be left empty (use wildcard ?)
 */
public class Machine<UP extends Protocol<?, ?>, DOWN extends MachineEventHandler<?,?>> extends MachineEventHandler<UP, DOWN> {
	double posX;
	double posY;
		
	double lastActualSendEnd = Double.NEGATIVE_INFINITY;
	
	private @NonNull Address address;
	
	/**
	 * Gets the address of the machine
	 * @return the address of the machine (never null)
	 */
	public @NonNull Address getAddress() {
		return address;
	}
	
	@Override
	protected void upcall(Event ev) {
		// If it has a port send only to the correct protocol otherwise send to all
		if (ev instanceof EventWithPort) {
			this.getUps().stream()
				.filter((up) -> up.getPort().equals(((EventWithPort)ev).getPort()))
				.forEach((up) -> {
					try {
						up.handle(ev);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				});
		} else {
			super.upcall(ev);
		}
	}
	
	/**
	 * This function handles any event.
	 * @param ev the event to handle
	 */
	@Override
	public void handle(Event ev) {
		this.upcall(ev);
	}
	
	/**
	 * Unicasts a message with some specified data using a sourcePort a destination address and port
	 * @param <T> The type of data to send
	 * @param sourcePort The source port from which to send the data
	 * @param destination The destination address
	 * @param destPort The destination port
	 * @param data The data to send
	 */
	public <T> void unicast(PublicKey sourcePort, Address destination, PublicKey destPort, T data) {
		double baseTime = Oracle.getInstance().getCurrentTimestamp();
		baseTime += Math.ulp(baseTime);
		
		Oracle.getInstance().getNetwork().unicast(this.getAddress(), sourcePort, destination, destPort, baseTime, data);
	}
	
	/**
	 * The position of the {@link Machine} in x as a number between [0,1]
	 * @return the x coordinate in [0,1] of the machine
	 */
	public double getPosX() {
		return posX;
	}
	/**
	 * The position of the {@link Machine} in y as a number between [0,1]
	 * @return the y coordinate in [0,1] of the machine
	 */	
	public double getPosY() {
		return posY;
	}

	/**
	 * Specifies the end of the last transmission from this node (used to detect collisions)
	 * @param lastActualSendEnd the end time of the transmission
	 */
	public void setLastActualSendEnd(double lastActualSendEnd) {
		this.lastActualSendEnd = lastActualSendEnd;
	}
	
	/**
	 * Get the timestamp of the last time this node finished to transmit
	 * @return the timestamp in seconds
	 */
	public double getLastActualSendEnd() {
		return lastActualSendEnd;
	}
	
	// For graphical purposes
	public String getGraphicAddress() {
		return this.address.toString();
	}
	
	/**
	 * Constructor for machine
	 * @param addr The address of the machine
	 * @param posX The position in x of the machine in [0,1]
	 * @param posY The position in y of the machine in [0,1]
	 */
	public Machine(@NonNull Address addr, double posX, double posY) {
		super(null);
		
		this.address = addr;
		
		this.posX = posX;
		this.posY = posY;
	}
	
	// -- Logging
	/**
	 * Log something for this machine using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, @NonNull String msg) {
		this.log(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}
	
	/**
	 * Log something for this machine using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, double timestamp, @NonNull String msg) {
		Oracle.getInstance().getLogger().getMachineLogger(this.getAddress()).print(System.out, msg, tag, ""+timestamp, this.getAddress().toString());
	}
	
	/**
	 * Log an error for this machine using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void err(@NonNull String tag, @NonNull String msg) {
		this.err(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}

	/**
	 * Log an error for this machine using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void err(@NonNull String tag, double timestamp, @NonNull String msg) {
		Oracle.getInstance().getLogger().getMachineLogger(this.getAddress()).print(System.err, msg, tag, ""+timestamp, this.getAddress().toString());
	}

}
