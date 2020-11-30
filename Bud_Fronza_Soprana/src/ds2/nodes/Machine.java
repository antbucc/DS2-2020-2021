package ds2.nodes;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.application.Application;
import ds2.application.ApplicationEvent;
import ds2.network.NetworkMessage;
import ds2.network.NetworkScheduler;
import ds2.nodes.events.BroadcastSend;
import ds2.protocol.Protocol;
import ds2.protocol.messages.Perturbation;
import ds2.simulator.Oracle;
import ds2.utility.Event;
import ds2.utility.Options;
import ds2.utility.Timestamped;

// Repast libraries
import repast.simphony.random.RandomHelper;

/**
 * This class represents a node of the network and especially its physical layer
 */
public class Machine {
	double posX;
	double posY;
		
	double lastActualSendEnd = Double.NEGATIVE_INFINITY;
	
	private @NonNull Address address;
	
	private @NonNull Protocol<?> protocol;
	private @NonNull Application<?> application;

	/**
	 * Gets the address of the machine
	 * @return the address of the machine (never null)
	 */
	public @NonNull Address getAddress() {
		return address;
	}
	
	/**
	 * Gets the protocol running on top of the machine
	 * @return the protocol running on top of the machine
	 */
	public @NonNull Protocol<?> getProtocol() {
		return protocol;
	}
	
	/**
	 * This functions allow us to set the protocol which sits on top of the machine and below the application
	 * @param protocol the protocol to use
	 */
	public void setProtocol(Protocol<?> protocol) {
		this.protocol = protocol;
	}
	
	/**
	 * This function allows us to set the application which sits on top of the protocol
	 * @param application
	 */
	public void setApplication(Application<?> application) {
		this.application = application;
	}
	
	/**
	 * This function handles an event.
	 * It will send {@link ApplicationEvent} to the application, it will execute the broadcast with a {@link BroadcastSend}, it will pass on the message to the protocol otherwise
	 * @param ev the event to handle
	 * @throws Exception Happens when sending unhandled types to the protocol
	 */
	public void handle(Event ev) throws Exception {
		if (ev instanceof ApplicationEvent) {
			this.application.handleApplicationEvent((ApplicationEvent) ev);
		} else if (ev instanceof BroadcastSend) {
			onBroadcastSend(new Timestamped<BroadcastSend>(Oracle.getInstance().getCurrentTimestamp(), (BroadcastSend) ev));
		} else {
			this.protocol.handle(ev);			
		}
	}
	
	/**
	 * The position of the {@link Machine} in x as a number between [0,1]
	 * @return
	 */
	public double getPosX() {
		return posX;
	}
	/**
	 * The position of the {@link Machine} in y as a number between [0,1]
	 * @return
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
	 * This is an utility function used to broadcast some data
	 * @param data the data to braodcast
	 */
	public void broadcast(Perturbation data) {
		double baseTime = Oracle.getInstance().getCurrentTimestamp() + Options.PROCESSING_DELAY + RandomHelper.nextDoubleFromTo(0, Options.MAX_RANDOM_DELAY);
		
		this.scheduleLocalEvent(baseTime, new BroadcastSend(this.address, data));
	}
	
	/**
	 * Handle function for {@link BroadcastSend} events
	 * @param toSend the timestamped {@link BroadcastSend} which contains the data
	 */
	private void onBroadcastSend(Timestamped<BroadcastSend> toSend) {	
		// If there is another send going on
		//   s     \____/    (this send)
		//   s \____/        (older send)
		if (this.getLastActualSendEnd() != Double.NEGATIVE_INFINITY &&
			NetworkScheduler.isCollision(toSend.getTimestamp() + Options.TRANSMISSION_TIME, this.getLastActualSendEnd())) {
			
			if (Options.DEBUG_COLLISIONS) {
				Oracle.mLog(this.getAddress(), "SC - Collision", "Send collision between " + toSend.getTimestamp() + Options.TRANSMISSION_TIME + " and " + this.getLastActualSendEnd());
			}
			
			// Add again with bigger timestamp (just after the NetworkMessage we are currently receiving)
			double newTimestamp = this.getLastActualSendEnd() + Options.PROCESSING_DELAY + RandomHelper.nextDoubleFromTo(0, Options.MAX_RANDOM_DELAY);
			this.scheduleLocalEvent(newTimestamp, toSend.getData());
		}
		
		NetworkScheduler net = Oracle.getInstance().getNetwork();
		
		Machine sourceMachine = this;

		Timestamped<NetworkMessage> tm = net.getFirstMessageOfAddr(this.address);
		
		// If there is no message next
		//   s \____/
		//   r               (empty)
		if (tm == null) {
			if (Options.DEBUG_COLLISIONS) {
				Oracle.mLog(this.getAddress(), "Collision", "N - Nothing after we can go on");
			}

			// As we are sending we set the end of the sending in machine so we can later recognize send + receive collisions
			this.setLastActualSendEnd(toSend.getTimestamp() + Options.TRANSMISSION_TIME);

			net.broadcast(this, toSend.getTimestamp(), toSend.getData().getPerturbation());
			return;
		}
		
		// If the beginning of the send is before the start of the next message (end - transimission_time) 
		//   (this means that at the time of send the node isn't receiving anything -> can send)
		//   s  \____/
		//   r         \____/
		// or
		//   s  \____/
		//   r	     \____/
		// or
		//   s  \____/
		//   r	   \____/
		if (toSend.getTimestamp() < tm.getTimestamp() - Options.TRANSMISSION_TIME) {
			if (Options.DEBUG_COLLISIONS) {
				Oracle.mLog(this.getAddress(), "Collision", "CT - Can trasmit immediatly");
			}
			
			// As we are sending we set the end of the sending in machine so we can later recognize send + receive collisions
			sourceMachine.setLastActualSendEnd(toSend.getTimestamp() + Options.TRANSMISSION_TIME);

			net.broadcast(this, toSend.getTimestamp(), toSend.getData().getPerturbation());
			return;
		}
		
		// If beginning of send is after the beginning of the next message
		//   s          \____/ | -> This shouldn't happen as the BroadcastEvent (ordered by begin) shouldn't be handled 
		//   r  \____/         | ->   before the received NetworkEvent (ordered by end) as we do them in order
		// or
		//   s     \____/
		//   r	\____/
		// or
		//   s       \____/
		//   r	\____/
		
		//  In all these cases we need to delay the send
		
		if (Options.DEBUG_COLLISIONS) {	
			Oracle.mLog(this.getAddress(), "Collision", "SW - Send has to wait");
		}
		
		// Add again with bigger timestamp (just after the NetworkMessage we are currently receiving)
		double newTimestamp = tm.getTimestamp() + Options.PROCESSING_DELAY + RandomHelper.nextDoubleFromTo(0, Options.MAX_RANDOM_DELAY);
		this.scheduleLocalEvent(newTimestamp, toSend.getData());
	}
	
	/**
	 * This function schedules a specified local event for this node at a specified timestamp
	 * @param timestamp the timestamp at which to schedule the event
	 * @param ev the event to schedule
	 */
	public void scheduleLocalEvent(double timestamp, LocalEvent ev) {
		Oracle.getInstance().getLocalEventsScheduler().schedule(timestamp, ev);
	}
	
	/**
	 * Constructor for machine
	 * @param addr The address of the machine
	 * @param posX The position in x of the machine in [0,1]
	 * @param posY The position in y of the machine in [0,1]
	 */
	public Machine(@NonNull Address addr, double posX, double posY) {
		this.address = addr;
		
		this.posX = posX;
		this.posY = posY;
	}
}
