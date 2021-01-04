package ds2.network;

// Standard libraries
import java.security.PublicKey;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.simulator.Oracle;
import ds2.simulator.QueuedScheduler;
import ds2.utility.Options;
import ds2.utility.Timestamped;

/**
 * This class represents the network. It thus contains all the messages that are currently in flight, orders them in order of reception, and provides some related utility functions
 */
public class NetworkScheduler extends QueuedScheduler<NetworkMessage<?>> {	
	/**
	 * Function to get the euclidian distance between 2 machines
	 * @param a The first machine
	 * @param b The second machine
	 * @return The euclidian distance between the two machines
	 */
	private static double getDistance(Machine<?, ?> a, Machine<?, ?> b) {
		double dx = a.getPosX() - b.getPosX();
		double dy = a.getPosY() - b.getPosY();
		
		return Math.sqrt( (dx*dx) + (dy*dy) );
	}
		
	/**
	 * With this function, given a source and baseTime at which we start to transmit, we can unicast the specified data 
	 * @param <T> The type of the data to send
	 * @param source The address of the machine that sends the data
	 * @param sourcePort The port from which the data is sent
	 * @param dest The address of the destination machine
	 * @param destPort The port of the destination
	 * @param baseTime The time at which the machine starts to transmit
	 * @param data The data transmitted
	 */
	public <T> void unicast(Address source, PublicKey sourcePort, Address dest, PublicKey destPort, double baseTime, @Nullable T data) {
		Machine<?, ?> sourceMachine = Oracle.getInstance().getMachine(source);
		Machine<?, ?> destMachine = Oracle.getInstance().getMachine(dest);
		
		double dist = NetworkScheduler.getDistance(sourceMachine, destMachine);
		
		double when = baseTime + dist/Options.PROPAGATION_SPEED + Options.TRANSMISSION_TIME;

		// We use reliable channels so the data is always received
		boolean received = true; 
		
		this.schedule(when, new NetworkMessage<T>(source, sourcePort, dest, destPort, received, data));
	}
	
	@Override
	public void update(Oracle oracle) {
		super.update(oracle);		
	}
	
	@Override
	public @Nullable Timestamped<NetworkMessage<?>> poll() {
		Timestamped<NetworkMessage<?>> ev = super.poll();
		
		// If the machine is not alive set received to false
		if (Oracle.getInstance().getMachine(ev.getData().getDestination()) == null) {
			ev.getData().setReceived(false);
		}

		return ev;
	}
}
