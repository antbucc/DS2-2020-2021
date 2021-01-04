package ds2.application;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.protocol.Protocol;
import ds2.simulator.Oracle;
import ds2.utility.logging.Logger;
import ds2.utility.logging.MachineEventHandler;

/**
 * This class represent the application on top of the protocol and must be extended into a real application in order to use it
 *
 * @param <UP> The type of the layer sitting on top of the application (usually no layers sits on top of an application so a wildcard can be used)
 * @param <DOWN> The type of layer onto which the application sits
 */
public abstract class Application<UP extends MachineEventHandler<?, ?>, 
								  DOWN extends Protocol<?, ?>> 
		extends MachineEventHandler<UP, DOWN> {
	/**
	 * Default constructor for the application
	 * @param down The layer onto which the application sits
	 */
	public Application(DOWN down) {
		super(down);
	}
	
	// -- Logging
	/**
	 * Log something for this application using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, @NonNull String msg) {
		this.log(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}

	/**
	 * Log something for this application using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, double timestamp, @NonNull String msg) {
		Oracle.getInstance().getLogger().getMachineLogger(this.getAddress()).print(System.out, msg, tag, ""+timestamp, this.getAddress().toString(), Logger.formatPort(this.getDown().getPort()));
	}

	/**
	 * Log an error for this application using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void err(@NonNull String tag, @NonNull String msg) {
		this.err(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}

	/**
	 * Log an error for this application using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void err(@NonNull String tag, double timestamp, @NonNull String msg) {
		Oracle.getInstance().getLogger().getMachineLogger(this.getAddress()).print(System.err, msg, tag, ""+timestamp, this.getAddress().toString(), Logger.formatPort(this.getDown().getPort()));
	}
}
