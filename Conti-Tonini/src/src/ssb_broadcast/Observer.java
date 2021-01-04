package ssb_broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import ssb_broadcast.Payload.PayloadType;

/**
 * Defines the Observer agent. This agent can emit and sense Payloads
 * carried by Perturbations.
 * 
 * The Observer contains an append-only log that stores every Payload received
 * and processed. The agent can query the log to reply ARQ requests received.
 * 
 * This class collects stats while the simulation is running. Please refer to
 * StatsManager.
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class Observer {
	// Defines the states of an Observer
	public enum ObserverStatus {
		// Is an active element of the gossip protocol.
		// Default value for statically allocated nodes.
		ACTIVE,
		// Do not react nor send messages.
		DEAD,
		// Recently joined the simulation, waiting for the first gossip request.
		// After first gossip exchange, NEW becomes ACTIVE.
		// Default for dynamically allocated nodes (i.e. during simulation).
		NEW
	}
	
	// Defines the probability to create an Event
	private final double createEventProbability;
	
	// Defines the probability to follow/unfollow or block/unblock an Observer
	private final double followBlockProbability;
	
	// Public key of the observer
	private final String publicKey;
	
	// Private key of the observer
	private final String privateKey;
	
	// Defines the continuous and grid objects of the 2D space
	private final ContinuousSpace<Object> space;
	private final Grid<Object> grid;
	
	// Append-only log structure
	private final Map<String, List<Integer>> perturbationLog;
	private int localPayloadIndex;
	
	// Store
	private final Store store;
	
	// Defines if observer has to send a "discover" payload
	private boolean needToDiscover = false;
	
	// Open/Transitive-Interest flag
	private final String gossipType;
	
	// Status
	private ObserverStatus status;
	
	// Color variables
	// Help us visually define active nodes (green), dead (red) or new (yellow)
	private double red = 0;
	private double green = 0;
	private double blue = 0;
	
	// Count the number of updates recorded
	private final List<Integer> updates;
	
	// Count the delay of each new perturbation sensed
	private final List<Integer> delays;
	
	private final Map<String, RepastEdge<Object>> follow;
	private final Map<String, RepastEdge<Object>> block;
	
	/**
	 * Initializes an Observer. This node is ACTIVE from the beginning of its life.
	 * 
	 * @param publicKey Public key of the observer
	 * @param privateKey Private key of the observer
	 * @param observersPublicKey Public key of all statically allocated nodes
	 * @param space Space object
	 * @param grid Grid object
	 * @param needToDiscover true if Observer has to send a "discovery" payload; otherwise, false
	 */
	public Observer(String publicKey, String privateKey, ContinuousSpace<Object> space, Grid<Object> grid, boolean needToDiscover) {
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.space = space;
		this.grid = grid;
		this.store = new Store(publicKey);
		this.createEventProbability = params.getDouble("create_event_probability");
		this.followBlockProbability = params.getDouble("follow_block_probability");
		this.perturbationLog = new HashMap<>();
		this.gossipType = params.getString("gossip_type");
		this.delays = new ArrayList<>();
		this.updates = new ArrayList<>();
		this.needToDiscover = needToDiscover;
		this.follow = new HashMap<>();
		this.block = new HashMap<>();
		
		if (this.needToDiscover) {
			// We need to spread our identifier on the network
			// Until a gossip is received, this observer is "new";
			// then it becomes "active"
			this.setStatus(ObserverStatus.NEW);
		} else {
			// If you init the observer with this constructor, the observer is immediately active
			this.setStatus(ObserverStatus.ACTIVE);
		}
	}
	
	public void setObservers(List<Observer> observers, Network<Object> network) {
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		int clique = params.getInteger("clique");
		
		// If gossip is transitive-interest, add a follow
		if (gossipType.toLowerCase().equals("transitive-interest")) {
			List<String> friends = new ArrayList<>();
			
			for (int i = 0; i < Math.min(clique, observers.size() - 1); i++) {
				Observer friend = null;
				do {
					int destinationIndex = RandomHelper.getUniform().nextIntFromTo(0, observers.size() - 1);
					friend = observers.get(destinationIndex);
				} while (friend == null || friend.getPublicKey() == this.publicKey || friends.contains(friend.getPublicKey()));
				
				friends.add(friend.getPublicKey());
				this.store.getLog(this.publicKey).follow(friend.getPublicKey(), this.privateKey);
				
				this.follow.put(friend.getPublicKey(), network.addEdge(this, friend));
			}
		}
		
		// Iterate every observer and add to log
		for (Observer observer: observers) {
			String observerPublicKey = observer.getPublicKey();
			
			this.store.add(new Log(observerPublicKey));
		}
	}
	
	public String getPublicKey() {
		return this.publicKey;
	}
	
	public String getId() {
		return this.publicKey.substring(this.publicKey.length() - 6);
	}
	
	public double getRed() {
		return red;
	}

	public double getGreen() {
		return green;
	}

	public double getBlue() {
		return blue;
	}
	
	public double getAverageUpdates() {
		if (this.updates.size() == 0) {
			return 0;
		}
		
		return this.updates.stream().mapToDouble(x -> x).average().getAsDouble();
	}
	
	public double getAverageDelays() {
		if (this.delays.size() == 0) {
			return 0;
		}
		
		return this.delays.stream().mapToDouble(x -> x).average().getAsDouble();
	}
	
	public int getTotalLocalEvents() {
		return this.store.getLog(this.publicKey).getLastEventIndex() + 1;
	}

	public int isActive() {
		return this.status == ObserverStatus.ACTIVE ? 1 : 0;
	}
	
	public int isNew() {
		return this.status == ObserverStatus.NEW ? 1 : 0;
	}
	
	public int isDead() {
		return this.status == ObserverStatus.DEAD ? 1 : 0;
	}
	
	public int getFollowed() {
		return this.store.getLog(this.getPublicKey()).getFollowed().size();
	}
	
	public int getBlocked() {
		return this.store.getLog(this.getPublicKey()).getBlocked().size();
	}
	
	public ObserverStatus getStatus() {
		return this.status;
	}
	
	public void setStatus(ObserverStatus status) {
		this.status = status;
		
		if (status == ObserverStatus.ACTIVE) {
			// Green
			this.red = 0;
			this.green = 1;
			this.blue = 0;
		} else if (status == ObserverStatus.NEW) {
			// Yellow
			this.red = 1;
			this.green = 1;
			this.blue = 0;
		} else if (status == ObserverStatus.DEAD) {
			// Red
			this.red = 1;
			this.green = 0;
			this.blue = 0;
		}
	}
		
	@ScheduledMethod(start = 0, interval = 250)
	public void log() {
		// Don't do anything if you are dead
		if (this.status == ObserverStatus.DEAD) {
			return;
		}
		
		// Determine if the Observer should create an Event
		if (RandomHelper.getUniform().nextDouble() > this.createEventProbability) {
			return;
		}
		
		// For a message, the content can be anything
		byte[] content = new byte[16];
		new Random().nextBytes(new byte[16]);
		
		this.store.getLog(this.publicKey).append(new String(content), this.privateKey);
	}
	
	@ScheduledMethod(start = 0, interval = 250)
	public void gossip() {
		// Don't do anything if you are dead
		if (this.status == ObserverStatus.DEAD) {
			return;
		}
		
		// If the Observer is new and no one knows
		// its existence, send a "discovery" message
		if (this.needToDiscover) {
			this.discover();
			return;
		}
		
		// Do not gossip if the store has only myself
		if (this.store.getIds().size() <= 1) {
			return;
		}
		
		// Find a destination
		String destinationPublicKey = null;
		do {
			int destinationIndex = RandomHelper.getUniform().nextIntFromTo(0, this.store.getIds().size() - 1);
			destinationPublicKey = this.store.getIds().get(destinationIndex);
		} while (destinationPublicKey == null || destinationPublicKey == this.publicKey);
	
		// Prepare the payload
		Payload payload = new Payload(this.publicKey, destinationPublicKey, this.store.clone(), this.localPayloadIndex++, PayloadType.REQUEST);
		
		// Log and forward
		this.addToLog(payload);
		this.forward(payload);
	}
	
	@ScheduledMethod(start = 0, interval = 200)
	public void doFollowUnfollowOrBlockUnblock() {
		// The open gossip protocol does not implement this behavior
		if (this.gossipType.toLowerCase().equals("open")) {
			return;
		}
		
		if (RandomHelper.getUniform().nextDouble() > this.followBlockProbability) {
			return;
		}
				
		// Do not gossip if the store has only myself
		if (this.store.getIds().size() <= 1) {
			return;
		}
		
		List<String> followed = this.store.getLog(this.publicKey).getFollowed();
		List<String> blocked = this.store.getLog(this.publicKey).getBlocked();
		
		String otherObserverPublicKey = null;
		do {
			int index = RandomHelper.getUniform().nextIntFromTo(0, this.store.getIds().size() - 1);
			otherObserverPublicKey = this.store.getIds().get(index);
		} while (otherObserverPublicKey == null || otherObserverPublicKey == this.publicKey);
		
		boolean isFollowed = followed.contains(otherObserverPublicKey);
		boolean isBlocked = blocked.contains(otherObserverPublicKey);
		
		boolean head = RandomHelper.getUniform().nextBoolean();
		
		if (isFollowed && !isBlocked) {
			// Block or unfollow
			this.unfollow(otherObserverPublicKey);
			if (head) {
				this.block(otherObserverPublicKey);
			}
		} else if (!isFollowed && isBlocked) {
			// Follow or unblock
			this.unblock(otherObserverPublicKey);
			if (head) {
				this.follow(otherObserverPublicKey);
			}
		} else {	
			// Follow or block
			if (head) {
				this.unblock(otherObserverPublicKey);
				this.follow(otherObserverPublicKey);
			} else {
				this.unfollow(otherObserverPublicKey);
				this.block(otherObserverPublicKey);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void follow(String otherObserverPublicKey) {
		this.store.getLog(this.publicKey).follow(otherObserverPublicKey, this.privateKey);
		
		// Find the observer with publicKey = otherObserverPublicKey
		Context<Object> context = ContextUtils.getContext(this);
		Observer thisObserver = null;
		
		for (Object object: context.getObjects(Observer.class)) {
			if (!(object instanceof Observer)) {
				continue;
			}
			
			Observer observer = (Observer)object;
			if (observer.getPublicKey().equals(otherObserverPublicKey)) {
				thisObserver = observer;
			}
		}
		
		Network<Object> network = (Network<Object>) context.getProjection("follow");
		
		this.follow.put(thisObserver.getPublicKey(), network.addEdge(this, thisObserver));
	}
	
	@SuppressWarnings("unchecked")
	public void unfollow(String otherObserverPublicKey) {
		this.store.getLog(this.publicKey).unfollow(otherObserverPublicKey, this.privateKey);
		
		RepastEdge<Object> edge = this.follow.get(otherObserverPublicKey);
		
		Context<Object> context = ContextUtils.getContext(this);
		Network<Object> network = (Network<Object>) context.getProjection("follow");
		network.removeEdge(edge);
		
		this.follow.remove(otherObserverPublicKey);
	}
	
	@SuppressWarnings("unchecked")
	public void block(String otherObserverPublicKey) {
		Context<Object> context = ContextUtils.getContext(this);
		Observer thisObserver = null;
		
		for (Object object: context.getObjects(Observer.class)) {
			if (!(object instanceof Observer)) {
				continue;
			}
			
			Observer observer = (Observer)object;
			if (observer.getPublicKey().equals(otherObserverPublicKey)) {
				thisObserver = observer;
			}
		}
		
		Network<Object> network = (Network<Object>) context.getProjection("block");
		this.block.put(otherObserverPublicKey, network.addEdge(this, thisObserver));
		
		this.store.getLog(this.publicKey).block(otherObserverPublicKey, this.privateKey);
	}
	
	@SuppressWarnings("unchecked")
	public void unblock(String otherObserverPublicKey) {
		this.store.getLog(this.publicKey).unblock(otherObserverPublicKey, this.privateKey);
		
		RepastEdge<Object> edge = this.block.get(otherObserverPublicKey);
		
		Context<Object> context = ContextUtils.getContext(this);
		Network<Object> network = (Network<Object>) context.getProjection("block");
		network.removeEdge(edge);
		
		this.block.remove(otherObserverPublicKey);
	}
	
	public void discover() {
		// Send a "Discovery" payload with my id
		this.needToDiscover = false;
		Payload payload = new Payload(this.publicKey, null, this.store, this.localPayloadIndex++, PayloadType.DISCOVERY);
		forward(payload);
	}
	
	public void sense(Payload payload, int delay) {
		// Don't do anything if you are dead
		if (this.status == ObserverStatus.DEAD) {
			return;
		}
		
		// Suppression mechanism
		if (this.perturbationLog.containsKey(payload.getSource()) &&
			this.perturbationLog.get(payload.getSource()).contains(payload.getRef())) {
			return;
		}
		
		// Log this payload
		this.addToLog(payload);
		
		// Record for statistics
		this.delays.add(delay);
		
		// Just an easy way to print keys
		String myPrettyPublicKey = this.publicKey.substring(this.publicKey.length() - 6);
		String yourPrettyPublicKey = payload.getSource().substring(payload.getSource().length() - 6);
		
		// Process iff I am the destination
		if (payload.getDestination() == this.publicKey) {
			if (this.gossipType.toLowerCase().equals("open")) {
				// Open gossip
				this.openGossip(payload.getVal());
			} else {
				// Transitive
				this.transitiveGossip(payload.getVal());
			}
			
			int updates = update(payload.getVal());
			System.out.format("[%s] Received %s from %s. %d new events received\n", myPrettyPublicKey, payload.getType(), yourPrettyPublicKey, updates);
			
			// Reply iff payload is a request
			if (payload.getType() == PayloadType.REQUEST) {
				// Change status if this is the first time I receive a gossip
				if (this.status == ObserverStatus.NEW) {
					this.setStatus(ObserverStatus.ACTIVE);
				}
				
				Payload replyPayload = new Payload(this.publicKey, payload.getSource(), this.store.clone(), this.localPayloadIndex++, PayloadType.REPLY);
				this.forward(replyPayload);
			}
		}
		else if (payload.getType() == PayloadType.DISCOVERY) {
			// System.out.format("[%s] Discovered %s \n", myPrettyPublicKey, yourPrettyPublicKey);
			
			// Found a new node, add it to the log
			this.store.add(new Log(payload.getSource()));
		}
	
		this.forward(payload);
	}	
	
	// Forward a broadcast message
	private void forward(Payload payload) {
		// Get the simulation context
		@SuppressWarnings("unchecked")
		Context<Object> context = ContextUtils.getContext(this);
		
		// System.out.format("[%d] Forwarding message from source %d with ref %d \n", this.id, payload.getSource(), payload.getRef());
		
		// Location of this observer
		GridPoint observerGridPoint = this.grid.getLocation(this);
		NdPoint observerNdPoint = this.space.getLocation(this);
		
		// Create a new perturbation
		Perturbation perturbation = new Perturbation(grid, payload);
		
		// Add the perturbation to the context and place it under the current observer
		context.add(perturbation);
		space.moveTo(perturbation, observerNdPoint.getX(), observerNdPoint.getY());
		grid.moveTo(perturbation, observerGridPoint.getX(), observerGridPoint.getY());
	}
	
	private void addToLog(Payload payload) {
		if (!this.perturbationLog.containsKey(payload.getSource())) {
			this.perturbationLog.put(payload.getSource(), new ArrayList<>());
		}
		
		// Add payload's ref to the log
		this.perturbationLog.get(payload.getSource()).add(payload.getRef());
	}
	
	// Open gossip protocol
	private void openGossip(Store remoteStore) {
		List<String> localIds = this.store.getIds();
		List<String> remoteIds = this.store.getIds();
		
		// By removing all localIds from remoteIds,
		// we end up with a list of new ids
		remoteIds.removeAll(localIds);
		
		for (String id: remoteIds) {
			this.store.add(new Log(id));
		}
	}
	
	// Transitive gossip protocol
	private void transitiveGossip(Store remoteStore) {
		List<String> localIds = this.store.getIds();
		List<String> followedIds = this.store.getFollowed(this.publicKey);
		List<String> blockedIds = this.store.getBlocked(this.publicKey);
		
		// followedIds = this.store.getFollowed - localIds
		followedIds.removeAll(localIds);
		blockedIds.retainAll(localIds);
		
		for (String id: followedIds) {
			this.store.add(new Log(id));
		}
		
		for (String id: blockedIds) {
			if (id.equals(this.publicKey)) {
				continue;
			}
			
			this.store.remove(id);
		}
	}
	
	private int update(Store remoteStore) {
		Map<String, Event> frontier = this.store.getFrontier();
		Map<String, List<Event>> news = remoteStore.getSince(frontier);
		int updates = this.store.update(news);
		
		// Record for stats purposes
		this.updates.add(updates);
		
		return updates;
	}
}
