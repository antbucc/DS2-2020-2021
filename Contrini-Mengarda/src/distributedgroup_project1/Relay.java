package distributedgroup_project1;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import distributedgroup_project1.messages.BroadcastMessage;
import distributedgroup_project1.messages.P2PMessage;
import distributedgroup_project1.messages.PubSubMessage;
import distributedgroup_project1.perturbations.ARQPerturbation;
import distributedgroup_project1.perturbations.BroadcastPerturbation;
import distributedgroup_project1.perturbations.P2PPerturbation;
import distributedgroup_project1.perturbations.Perturbation;
import distributedgroup_project1.perturbations.PubSubPerturbation;
import distributedgroup_project1.perturbations.RetransmissionPerturbation;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.engine.watcher.Watch;
import repast.simphony.engine.watcher.WatcherTriggerSchedule;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.Pair;

public class Relay {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	// ID of the relay
	private int id;
	// Incrementing counter of the next perturbation's "ref" ID
	private int ref;

	// The RSA private key used for encryption in P2P mode
	private final RSAPrivateKey privateKey;
	
	// The log of received perturbations, for each source (ID, which is an Integer) 
	private Map<Integer, List<Perturbation>> log = new HashMap<>();
	
	// HashMap used to keep track of how many duplicate perturbations were retransmitted,
	// for analysis purposes. A perturbation is considered duplicate when it has the same
	// "source" and "ref" 
	private Map<Pair<Integer, Integer>, Integer> dups = new HashMap<>();
	
	// A circular buffer to store the transmission time (in ticks) of the latest 50 perturbations
	private double[] latenciesBuffer = new double[50];
	// Helper variable to keep track of the next cell to fill/overwrite in the circular buffer
	private int latenciesBufferLastIndex;

	/**
	 * Constructor of the class Relay.
	 * 
	 * @param space: the continuous space where the relay will be displayed
	 * @param grid:  the grid where the relay will be located
	 * @param id:    unique field that identifies a relay
	 */
	public Relay(ContinuousSpace<Object> space, Grid<Object> grid, int id, RSAPrivateKey privateKey) {
		this.space = space;
		this.grid = grid;
		this.id = id;
		this.ref = 0;
		this.privateKey = privateKey;		

		// Fill the buffer with -1 so that we know which cells actually contain data 
		Arrays.fill(latenciesBuffer, -1);
	}

	/**
	 * Method to create and send a new perturbation, called periodically by the runtime.
	 * The destination and the content of the perturbation message is chosen randomly.
	 * The perturbation is then appended to the logs and broadcasted.
	 * 
	 * It should be an API provided to the application layer.
	 */
	@ScheduledMethod(start = 0, interval = 10)
	public void createAndSendPerturbation() {
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		double broadcastProbability = Utils.getParams().getDouble("perturbation_probability");
		
		if (random < broadcastProbability) {
			Perturbation perturbation = createPerturbation();
			appendToLog(perturbation);
			sendPerturbation(perturbation);
		}
	}
	
	/**
	 * Method to create a Perturbation based on runtime parameters.
	 * The perturbation can be P2PPerturbation, PubSubPerturbation or BroadcastPerturbation.
	 * The size of the message is also taken from a runtime parameter.
	 * 
	 * @return: the created perturbation
	 */
	public Perturbation createPerturbation() {
		// Get the probability that the perturbation should be a Point-To-Point perturbation.
		// For a random value picked in the [0, 1] range, the P2P cases would be in the range [0, p2pProbability] 
		double p2pProbability = Utils.getParams().getDouble("p2p_probability");
		// Get the PubSub probability
		// In a [0, 1] range, the PubSub cases would be in the range (p2pProbability, pubSubProbability].
		// The normal broadcast probability would be (pubSubProbability, 1] 
		double pubSubProbability = Utils.getParams().getDouble("pubsub_probability") + p2pProbability;
		
		// Generate a random content for the perturbation,
		// taking into account the message size runtime parameter
		int size = Utils.getParams().getInteger("message_size");
		String content = Utils.getRandomString(size);
		Perturbation perturbation;
		
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		if (random <= p2pProbability) {
			// In Point-To-Point communication, choose a random relay as a destination. 
			int dest = Utils.getRandomRelayId();
			
			// Take the destination's public key and encrypt the message content with it
			RSAPublicKey publicKey = Utils.getPublicKeyByRelayId(dest);
			P2PMessage message = new P2PMessage(Utils.encrypt(content, publicKey), size);
			perturbation = new P2PPerturbation(space, grid, this.id, this.ref++, message);
		} else if (random <= pubSubProbability) {
			// In Publish-Subscribe, choose a random topic to multicast the message to
			PubSubMessage message = new PubSubMessage(Utils.getRandomTopic(), content, size);
			perturbation = new PubSubPerturbation(space, grid, this.id, this.ref++, message);			
		} else {
			// Classic broadcast
			BroadcastMessage message = new BroadcastMessage(content, size);
			perturbation = new BroadcastPerturbation(space, grid, this.id, this.ref++, message);
		}
		
		return perturbation;
	}

	/**
	 * Method to broadcast a perturbation in the continuous space.
	 * Broadcasting a perturbation in this implementation means placing the perturbation
	 * in the space, in the same position as the sender.
	 * The perturbation will then expand by itself with time and be intercepted by other relays.
	 * 
	 * @param perturbation: the perturbation to be sent
	 */
	private void sendPerturbation(Perturbation perturbation) {
		Context<Object> context = Utils.getContext(this);
		context.add(perturbation);
		NdPoint spacePoint = space.getLocation(this);
		GridPoint gridPoint = grid.getLocation(this);
		space.moveTo(perturbation, spacePoint.getX(), spacePoint.getY());
		grid.moveTo(perturbation, gridPoint.getX(), gridPoint.getY());
	}

	/**
	 * Method to append a perturbation to the local log of the relay.
	 * 
	 * @param perturbation: the perturbation to appended to the log
	 */
	private void appendToLog(Perturbation perturbation) {
		int source = perturbation.getSource();
		
		// The log is a Map keyed by source relay, so the first time we need to initialize
		// the list corresponding to the given source
		if (!log.containsKey(source)) {
			log.put(source, new ArrayList<>());
		}
		
		log.get(source).add(perturbation);
	}

	/**
	 * Method to obtain the "ref" of the next perturbation that is expected from a given source.
	 * 
	 * @param source: the source for which the next ref is requested
	 * @return the integer "ref" value that is expected
	 */
	private int getNextRef(int source) {
		// If no perturbation has been received from the source,
		// the first perturbation (with ref=0) has still to be received
		if (!log.containsKey(source)) {
			log.put(source, new ArrayList<>());
			return 0;
		}
		
		if (log.get(source).size() == 0) {
			return 0;
		}
		
		// Take the last perturbation that was logged and return its ref + 1
		Perturbation lastReceived = log.get(source).get(log.get(source).size() - 1);
		return lastReceived.getRef() + 1;
	}

	/**
	 * Method that is called whenever a perturbation is in the range of the relay.
	 * If the perturbation has actually reached the relay, it is processed.
	 * 
	 * @param perturbation: the perturbation that is sensed by the relay
	 */
	@Watch(watcheeClassName = "distributedgroup_project1.perturbations.Perturbation",
		   // Watch the "radius" of the Perturbation, which defines how much it has spread in the space
		   watcheeFieldNames = "radius",
		   // Call the method only when the perturbation is close enough to the relay to be sensed
		   query = "within " + Utils.MAX_PERTURBATION_RADIUS,
		   whenToTrigger = WatcherTriggerSchedule.IMMEDIATE)
	public void onPerturbation(Perturbation perturbation) {
		// Do not process the perturbations that I sent or that I have already handled.
		// The second condition is needed because this method keeps getting called
		// even after the perturbation has reached (and went beyond) the relay
		if (perturbation.getSource() == id || perturbation.hasBeenProcessed(this.id)) {
			return;
		}
		
		// Compute the distance between the relay and the circumference of the perturbation
		NdPoint pos1 = this.space.getLocation(perturbation);
		NdPoint pos2 = this.space.getLocation(this);
		double distance = this.space.getDistance(pos1, pos2) - perturbation.getRadius();
		
		if (distance <= 0) {
			// Memorize the fact that the perturbation has reached this particular relay
			perturbation.markAsProcessed(this.id);
			
			if (perturbation instanceof ARQPerturbation) {
				// Manage Automatic Repeat reQuests
				manageARQRequest(perturbation);
			} else {
				// Manage other types of perturbations
				manageMessage(perturbation);
			}
		}
	}

	/**
	 * Method to handle a broadcast perturbation. If it is the expected
	 * perturbation, it is logged and forwarded.
	 * 
	 * @param perturbation: the perturbation sensed by the relay
	 */
	private void manageMessage(Perturbation perturbation) {
		// If this is the perturbation that I'm expecting from this source
		if (perturbation.getRef() == getNextRef(perturbation.getSource())) { 
			appendToLog(perturbation);
			forwardPerturbation(perturbation);
			
			// Deliver the perturbation/message if I'm one of the recipients
			if (shouldDeliver(perturbation)) {
				deliver(perturbation);				
			}
		}
	}
	
	/**
	 * Method to check if the received perturbation needs to be delivered
	 * 
	 * @param perturbation: the perturbation sensed by the relay
	 * @return: true if the perturbation needs to be delivered, false otherwise
	 */
	private boolean shouldDeliver(Perturbation perturbation) {
		// Always deliver broadcast perturbations
		if (perturbation instanceof BroadcastPerturbation) {
			return true;
		}
		// Deliver Point-To-Point perturbations only if I'm the destination relay.
		// The message decryption implicitly acts as a check because only the destination relay
		// has the private key required to decrypt a P2P message.
		else if (perturbation instanceof P2PPerturbation &&
				Utils.decrypt(perturbation.getMessage().getVal(), this.privateKey) != null) {
			return true;
		}
		// Deliver PubSub perturbations only if I'm subscribed to the message topic
		else if (perturbation instanceof PubSubPerturbation && isSubscribed(perturbation)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Method to check if the current relay is subscribed to the topic of
	 * the message carried by the given perturbation
	 * 
	 * @param perturbation: the perturbation sensed by the relay
	 * @return: true if the perturbation needs to be delivered, false otherwise
	 */
	private boolean isSubscribed(Perturbation perturbation) {
		int topic = ((PubSubMessage) perturbation.getMessage()).getTopic();
		// Use the modulo operator to "randomly" but deterministically subscribe relays to topics 
		return (this.id + 1) % topic == 0;
	}
	
	/**
	 * Method called when a perturbation/message should be delivered to the application layer.
	 * 
	 * In this simulation, the method simply computes the latency, that is the time that
	 * elapsed between the the perturbation broadcast from the source and the delivery in this relay.
	 * 
	 * @param perturbation: the perturbation to deliver
	 */
	public void deliver(Perturbation perturbation) {
		double latency = Utils.getCurrentTick() - perturbation.getCreationTick();
		// The latency is stored in a circular buffer for analysis purposes
		latenciesBuffer[latenciesBufferLastIndex] = latency;
		latenciesBufferLastIndex = (latenciesBufferLastIndex + 1) % latenciesBuffer.length;
	}

	/**
	 * Method to handle an Automatic Repeat reQuest perturbation. If the requested
	 * perturbation is present in the logs, it is retransmitted to the other relays.
	 * 
	 * @param perturbation: the perturbation with the request
	 */
	private void manageARQRequest(Perturbation perturbation) {
		int source = perturbation.getSource();
		int ref = perturbation.getRef();
		
		if (log.containsKey(source)) {
			// Look for the requested perturbation using the
			// source+ref values contained in the perturbation
			Perturbation requested = log.get(source).stream()
					.filter(p -> p.getRef() == ref).findFirst().orElse(null);
			
			if (requested != null) {
				updateDuplicateRetransmissionsCount(requested);
				sendPerturbation(RetransmissionPerturbation.from(requested));
			}
		}
	}
	
	/**
	 * Method to update the count of duplicate perturbations, for analysis purposes.
	 * A perturbation is repeated if it's sent as a retransmission after an ARQPerturbation.
	 * 
	 * @param perturbation: the perturbation sensed by the relay
	 */
	private void updateDuplicateRetransmissionsCount(Perturbation perturbation) {
		Pair<Integer, Integer> key = new Pair<Integer, Integer>(
			perturbation.getSource(),
			perturbation.getRef()
		);
		
		if (dups.containsKey(key)) {
			dups.put(key, dups.get(key) + 1); 			
		} else {
			dups.put(key, 1);
		}
	}
	
	/**
	 * Method to forward a perturbation. The perturbation is cloned and then sent.
	 * 
	 * @param perturbation: the perturbation to be forwarded
	 */
	private void forwardPerturbation(Perturbation perturbation) {
		Perturbation newPerturbation = perturbation.clone();
		sendPerturbation(newPerturbation);
	}

	/**
	 * Method to perform ARQ requests. Invoked periodically by the runtime.
	 */
	@ScheduledMethod(start = 200, interval = 200)
	public void sendARQ() {
		// For each source in the log...
		log.keySet().forEach(source -> {
			// Request the next perturbation that I might have missed
			int nextRef = getNextRef(source);
			Perturbation perturbation = new ARQPerturbation(space, grid, source, nextRef, null);
			sendPerturbation(perturbation);
		});
	}

	/**
	 * Method that returns the id of the relay.
	 * 
	 * @return the id of the relay
	 */
	public int getId() {
		return id;
	}

	/**
	 * Method that returns the total size of the log.
	 * 
	 * @return the total size of the log of the relay
	 */
	public int getLogCount() {
		// This MapReduce operation produces the sum of the sizes of all the logs (one per source) 
		return log.values().stream().map(x -> x.size()).reduce((x, y) -> x + y).orElse(0);	
	}

	/**
	 * Method that returns the total number of duplicate perturbations.
	 * 
	 * @return the total number of duplicate perturbations
	 */
	public int getDupsCount() {
		// Simply sums all the values in the Map
		return dups.values().stream().reduce((x, y) -> x + y).orElse(0);	
	}
	
	/**
	 * Method to compute the mean of the latencies recorded in the latenciesBuffer.
	 * 
	 * @return: the average latency of the perturbations delivered by the relay 
	 */
	public double getAverageLatency() {
		double sum = 0;
		int count = 0;
		
		for (int i = 0; i < latenciesBuffer.length; i++) {
			// Since this is a fixed-size circular buffer, initially values
			// are initialized to -1. Therefore, after we encounter a negative number we can conclude
			// that the rest of the array is "empty" as well, and we can stop here.
			if (latenciesBuffer[i] < 0) {
				break;
			}
			
			sum += latenciesBuffer[i];
			count += 1;
		}
		
		return count > 0 ? sum / count : 0;
	}
}
