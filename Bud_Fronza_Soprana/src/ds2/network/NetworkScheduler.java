package ds2.network;

//Standard libraries
import java.util.Optional;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.protocol.messages.Perturbation;
import ds2.simulator.Oracle;
import ds2.simulator.QueuedScheduler;
import ds2.utility.Options;
import ds2.utility.Timestamped;

// Repast libraries
import repast.simphony.random.RandomHelper;

/**
 * This class represents the network. It thus contains all the messages that are currently in flight, orders them in order of reception and provides some related utility functions
 */
public class NetworkScheduler extends QueuedScheduler<NetworkMessage> {	
	/**
	 * Function to get the euclidian distance between 2 machines
	 * @param a The first machine
	 * @param b The second machine
	 * @return The euclidian distance between the two machines
	 */
	private static double getDistance(Machine a, Machine b) {
		double dx = a.getPosX() - b.getPosX();
		double dy = a.getPosY() - b.getPosY();
		
		return Math.sqrt( (dx*dx) + (dy*dy) );
	}
	
	/**
	 * This function checks, given the start and end time of two messages, if this would collide
	 * @param startTime1 The starting time of the first message
	 * @param endTime1 The end time of the first message
	 * @param startTime2 The starting time of second message
	 * @param endTime2 The end time of the second message
	 * @return true if they messages would collide, false otherwise
	 */
	public static boolean isCollision(double startTime1, double endTime1, double startTime2, double endTime2) {
		/* Collision example
		 *      \________/
		 *           \________/
		 *           |   |
		 *         max   min
		 *  (of start)   (of end)
		 *  
		 * No collision example
		 *  \________/
		 *              \________/
		 *           |  |
		 *         min  max
		 *    (of end)  (of begin)     
		 *      
		 */
		return Math.max(startTime1, startTime2) <= Math.min(endTime1, endTime2);
	}
	
	/**
	 * This function checks, given end time of two messages, if this would collide. The start times of the two messages are assumed to be the end time - the transmission time
	 * @param endTime1 End time of the first message
	 * @param endTime2 End time of the second messagte
	 * @return true if the messages would collide, false otherwise
	 */
	public static boolean isCollision(double endTime1, double endTime2) {
		return isCollision(endTime1 - Options.TRANSMISSION_TIME, endTime1, endTime2 - Options.TRANSMISSION_TIME, endTime2);
	}
	
	/**
	 * Internal utility function to generate a reception event for a specific node using the specified broadcasted message.
	 * This will take into account the distance and the related latency and packet loss
	 *  (Note: If the destination is farther then NORMAL_DIST*CUT_OFF no reception event will be generated)
	 * @param source The source of the broadcast
	 * @param destination The node that should receive this message
	 * @param baseTime The time at which the source starts to send the message
	 * @param data The data sent
	 */
	private void unicast_send(Machine source, Machine destination, double baseTime, @Nullable Perturbation data) {
		double dist = NetworkScheduler.getDistance(destination, source);
		
		boolean received = true;

		if (dist > Options.NORMAL_DIST * Options.CUT_OFF) {
			// Don't even try, the message won't be received the node is way too far (needed as double can get very small and have over/underflowing issues)
			received = false;
		} else if (dist != 0) {
			// Model from paper cited + Wikipedia (some experimental parameters)
			// Get SNR taking into account the distance
			double true_snr = Options.SNR - 10*2.6*Math.log10(dist/Options.NORMAL_DIST) - RandomHelper.createNormal(0, Options.VAR_NOISE).nextDouble();
			// Get probability of receiving given SNR and packet size
			double prob = Math.pow(1 - 0.5*Math.exp(-true_snr/2), 8*Options.PACKET_SIZE);
			
			received = prob < RandomHelper.nextDoubleFromTo(0, 1);

			double when = baseTime + dist/Options.PROPAGATION_SPEED + Options.TRANSMISSION_TIME;

			this.schedule(when, new NetworkMessage(source.getAddress(), destination.getAddress(), received, data));
		}		
	}
	
	/**
	 * With this function, given a source and baseTime at which we start to transmit, we can broadcast the specified data 
	 * @param sourceMachine The machine that broadcasts
	 * @param baseTime The time at which the machine starts to transmit
	 * @param data The data transmitted
	 */
	public void broadcast(Machine sourceMachine, double baseTime, @Nullable Perturbation data) {
		Oracle.getInstance()
		.getAllMachines()
		.stream()
		.filter(m -> m != sourceMachine)
		.forEach(destinationMachine -> {
			this.unicast_send(sourceMachine, destinationMachine, baseTime, data);
		});			
	}

	/**
	 * Update the queue of receive events ({@link NetworkMessage}) to have the ones with collisions with the field received as false
	 */
	private void updateCollisions() {
		Timestamped<NetworkMessage> first = this.peek();	

		if (first == null)
			return;
		
		Machine receiverMachine = Oracle.getInstance().getMachine(first.getData().getDestination());
		
		// If this machine is no longer alive, ignore all collisions (messages wouldn't be received anyway)
		if (receiverMachine == null)
			return;
		
		if (isCollision(receiverMachine.getLastActualSendEnd(), first.getTimestamp())) {
			// Given end of transmission of last send and end of receiving of message we can say (given the length of both is TRANSMISSION_TIME)
			//   we can know if they collided, if yes, set listened message as not received (as is not possible to listen while transmitting)
			first.getData().received = false;
		
			if (Options.DEBUG_COLLISIONS) {
				Oracle.mLog(first.getData().getDestination(), "Collision", first.getTimestamp(),"RS - Received during send");
			}
		}
		
		Optional<Timestamped<NetworkMessage>> nextForSameDest = this.queue
				.stream()
				.filter(t -> t.getData().getDestination().equals(first.getData().getDestination()))
				.skip(1)
				.findFirst();
		
		if ( !nextForSameDest.isPresent())
			return;
		
		Timestamped<NetworkMessage> next = nextForSameDest.get();
		if (isCollision(first.getTimestamp(), next.getTimestamp()) ) {
			// If we have two messages for the same destination close enough in time to collide set both as not received
			first.getData().received = false;
			next.getData().received = false;

			if (Options.DEBUG_COLLISIONS) {
				Oracle.mLog(first.getData().getDestination(), "Collision", first.getTimestamp(), "TR - Two receive at same time"); // TODO: Count as two?
			}
		}
	}

	/**
	 * Get the first message to be received or not by the machine with the specified address
	 * @param addr The address of the machine of which we have to find the first message to be received or not
	 * @return The timestamped message if it exists, null otherwise
	 */
	public @Nullable Timestamped<NetworkMessage> getFirstMessageOfAddr(Address addr) {
		return this.queue
				.stream()
				.filter(t -> t.getData().getDestination().equals(addr))
				.findFirst()
				.orElse(null);
	}
	
	@Override
	public void update(Oracle oracle) {
		super.update(oracle);		
	}
	
	@Override
	public @Nullable Timestamped<NetworkMessage> poll() {
		this.updateCollisions();		

		return super.poll();
	}
}
