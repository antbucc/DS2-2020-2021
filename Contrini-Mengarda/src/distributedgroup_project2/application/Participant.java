package distributedgroup_project2.application;

import java.util.List;
import java.util.Set;

import distributedgroup_project2.PrivateKey;
import distributedgroup_project2.PublicKey;
import distributedgroup_project2.Utils;
import distributedgroup_project2.application.NetworkEdge.EdgeType;
import distributedgroup_project2.transport.Relay;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

public class Participant extends Relay {

	private final Store store;
	private final PublicKey publicKey;
	private final PrivateKey privateKey;
	
	private int newUpdatesCount;

	/**
	 * Constructor of the class Participant.
	 * 
	 * @param space: the continuous space where the Participant (Relay) will be displayed;
	 * @param id: unique field that identifies a Participant (Relay);
	 * @param publicKey: the public key of the participant;
	 * @param privateKey: the private key of the participant;
	 * @param existsFromBeginning: flag to specify if a participant has been added while the execution was in progress.
	 */
	public Participant(ContinuousSpace<Object> space, int id, PublicKey publicKey,
			PrivateKey privateKey, boolean existsFromBeginning) {
		super(space, id, existsFromBeginning);

		this.publicKey = publicKey;
		this.privateKey = privateKey;

		this.store = new Store(publicKey);
		this.store.add(new Log(publicKey));
	}

	/**
	 * Method to initialize the "friends" network.
	 * 
	 * @param friends: a set of friends.
	 */
	public void initFriends(Set<Participant> friends) {
		Network<Object> network = Utils.getNetwork(this);
		
		friends.forEach(friend -> {
			// Add an edge to the network for each participant this participant is following
			network.addEdge(new NetworkEdge(EdgeType.FOLLOW, this, friend));
			
			// Create the store for the friend
			this.store.add(new Log(friend.getPublicKey()));
			
			// Add a follow event for the participant
			this.store.get(publicKey).follow(privateKey, friend.getPublicKey());
		});
	}

	/**
	 * Method to retrieve the public key of the participant.
	 * 
	 * @return: the public key of the participant.
	 */
	public PublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Method to generate a new generic event with a certain probability.
	 */
	@ScheduledMethod(start = 0, interval = 5)
	public void generateEvent() {
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		double eventProbability = Utils.getParams().getDouble("event_probability");

		if (random < eventProbability) {
			// Add a new event (with a fixed content) to the store
			this.store.get(publicKey).append(privateKey, "content");
			
			// Send a gossip message to let others know
			gossip();
		}
	}

	/**
	 * Method to broadcast the latest version of the store.
	 */
	@ScheduledMethod(start = 200, interval = 200)
	public void gossip() {
		super.broadcast(this.store);
	}

	/**
	 * Method to probabilistically generate new events for managing friends.
	 * The possible events types are follow, unfollow, block and unblock.
	 */
	@ScheduledMethod(start = 50, interval = 50)
	public void manageFriends() {
		String gossipType = Utils.getParams().getString("gossip_type");
		if (gossipType.equals("OpenGossip")) {
			// Do not execute if I'm in open gossip
			return;
		}
		
		double random;
		double followProbability = Utils.getParams().getDouble("follow_probability");
		double unfollowProbability = Utils.getParams().getDouble("unfollow_probability");
		double blockProbability = Utils.getParams().getDouble("block_probability");
		double unblockProbability = Utils.getParams().getDouble("unblock_probability");

		boolean shouldGossip = false;

		Network<Object> network = Utils.getNetwork(this);

		random = RandomHelper.nextDoubleFromTo(0, 1);
		
		if (random <= followProbability) {
			Set<PublicKey> ids = this.store.getIds();
			Set<PublicKey> followed = this.store.getFollowed(this.publicKey);

			// From the participants I know, remove all the participants I already follow
			ids.removeAll(followed);
			// And myself
			ids.remove(this.publicKey);

			// Choose a random one
			Participant newFriend = Utils.getRandomParticipant(ids);

			// If it's still in the context, follow it and add the edge in the network
			if (newFriend != null && Utils.getContext(this).contains(newFriend)) {
				this.store.get(this.publicKey).follow(privateKey, newFriend.getPublicKey());

				network.addEdge(new NetworkEdge(EdgeType.FOLLOW, this, newFriend));
				shouldGossip = true;

				Utils.log(this + " has followed " + newFriend);
			}
		}

		random = RandomHelper.nextDoubleFromTo(0, 1);
		
		// Take a random participant I'm already following and unfollow it, by also removing the edge
		if (random <= unfollowProbability) {
			Set<PublicKey> followed = this.store.getFollowed(publicKey);

			Participant unfollowedFriend = Utils.getRandomParticipant(followed);

			if (unfollowedFriend != null) {
				this.store.get(publicKey).unfollow(privateKey, unfollowedFriend.getPublicKey());

				network.removeEdge(network.getEdge(this, unfollowedFriend));
				shouldGossip = true;
				Utils.log(this + " has unfollowed " + unfollowedFriend);
			}
		}

		random = RandomHelper.nextDoubleFromTo(0, 1);
		
		if (random <= blockProbability) {
			Set<PublicKey> ids = this.store.getIds();
			Set<PublicKey> followed = this.store.getFollowed(publicKey);
			Set<PublicKey> blocked = this.store.getBlocked(publicKey);

			// Take all the participants I already know, remove the ones I already blocked
			ids.removeAll(blocked);
			// Remove the ones I'm following
			ids.removeAll(followed);
			// Remove myself
			ids.remove(this.publicKey);

			// Proceed to block a random one
			Participant newBlocked = Utils.getRandomParticipant(ids);

			if (newBlocked != null && Utils.getContext(this).contains(newBlocked)) {
				this.store.get(publicKey).block(privateKey, newBlocked.getPublicKey());
				this.store.remove(newBlocked.getPublicKey());

				network.addEdge(new NetworkEdge(EdgeType.BLOCK, this, newBlocked));
				shouldGossip = true;
				Utils.log(this + " has blocked " + newBlocked);
			}
		}

		random = RandomHelper.nextDoubleFromTo(0, 1);
		
		// Take a random participant I'm already blocking and unblock it, by also removing the edge
		if (random <= unblockProbability) {
			Set<PublicKey> blocked = this.store.getBlocked(publicKey);

			Participant unblockedFriend = Utils.getRandomParticipant(blocked);

			if (unblockedFriend != null) {
				this.store.get(publicKey).unblock(privateKey, unblockedFriend.getPublicKey());
				this.store.add(new Log(unblockedFriend.getPublicKey()));

				network.removeEdge(network.getEdge(this, unblockedFriend));
				shouldGossip = true;
				Utils.log(this + " has unblocked " + unblockedFriend);
			}
		}

		// If something changed, gossip the store to let others know
		if (shouldGossip) {
			gossip();
		}
	}

	/**
	 * Method to handle the delivery of a message from the transport layer.
	 * This method is called by the super class (transport layer).
	 *
	 * @param message: the message delivered by the transport layer.
	 */
	@Override
	public void deliver(Object message) {
		Store otherStore = (Store) message;
		String gossipType = Utils.getParams().getString("gossip_type");
		
		if (gossipType.equals("OpenGossip")) {
			openGossipDeliver(otherStore);
		} else {
			transitiveInterestGossipDeliver(otherStore);
		}
	}

	/**
	 * Method to execute the OpenGossip protocol.
	 *
	 * @param otherStore: the incoming store coming from another participant.
	 */
	private void openGossipDeliver(Store otherStore) {
		// Create the missing logs
		otherStore.getIds().forEach(id -> {
			store.add(new Log(id));
		});

		// Update the logs with the new events
		update(otherStore);
	}

	/**
	 * Method to execute the Transitive-Interest protocol.
	 *
	 * @param otherStore: the incoming store coming from another participant.
	 */
	private void transitiveInterestGossipDeliver(Store friend) {
		Set<PublicKey> myFollowed = this.store.getFollowed(this.publicKey);

		// If the store I'm receiving is from a participant I'm not following,
		// update my store with new events but don't execute the transitive-interest algorithm
		if (!myFollowed.contains(friend.getOwnerPublicKey())) {
			update(friend);
			return;
		}

		Set<PublicKey> ids = this.store.getIds();
		Set<PublicKey> followed = this.store.getFollowed(friend.getOwnerPublicKey());
		Set<PublicKey> blocked = this.store.getBlocked(friend.getOwnerPublicKey());

		// Add logs for all the friends of my friend
		followed.removeAll(ids);
		followed.forEach(participant -> this.store.add(new Log(participant)));

		// From the participants my friend has blocked, consider only the ones I know
		blocked.retainAll(ids);
		// Remove the ones I'm following
		blocked.removeAll(myFollowed);
		// And myself...
		blocked.remove(this.publicKey);
		// Remove the logs for the blocked
		blocked.forEach(participant -> this.store.remove(participant));

		update(friend);
	}

	/**
	 * Method to update the local store upon a new delivery.
	 *
	 * @param otherStore: the incoming store.
	 */
	private void update(Store otherStore) {
		// Get the frontier for each log I have
		Frontier localFrontier = this.store.getFrontier(this.store.getIds());
		
		// Extract the new events from the incoming store
		List<Event> news = otherStore.since(localFrontier);
		
		// Insert the new events in my local logs
		this.store.update(news);
		
		this.newUpdatesCount = news.size();
	}

	/**
	 * Method to retrieve the string representation of the participant (public key).
	 *
	 * @return: the String representation of the participant.
	 */
	@Override
	public String toString() {
		return this.publicKey.toString();
	}

	/**
	 * Method to retrieve the number of followed participants.
	 * Used in charts.
	 *
	 * @return: the number of followed participants.
	 */
	public int getFollowedCount() {
		return this.store.getFollowed(this.publicKey).size();
	}

	/**
	 * Method to retrieve the number of blocked participants.
	 * Used in charts.
	 * 
	 * @return: the number of blocked participants.
	 */
	public int getBlockedCount() {
		return this.store.getBlocked(this.publicKey).size();
	}

	/**
	 * Method to retrieve the label of the participant displayed in the Repast GUI.
	 *
	 * @return: the label of the participant.
	 */
	public String getLabel() {
		String gossipType = Utils.getParams().getString("gossip_type");
		if (gossipType.equals("OpenGossip")) {
			return String.valueOf(super.getId());
		} else {
			return getFollowedCount() + " | " + getBlockedCount();			
		}
	}

	/**
	 * Method to retrieve number of known participants.
	 * Used in charts.
	 *
	 * @return: the number of known participants.
	 */
	public int getKnownParticipantsCount() {
		// Return the store size minus 1 to avoid counting myself
		return this.store.size() - 1;
	}

	/**
	 * Method to retrieve number of total events in the store.
	 * Used in charts.
	 *
	 * @return: the number of total events in the store.
	 */
	public int getTotalEventsCount() {
		int eventsCount = 0;
		
		for (Log log : this.store.values()) {
			eventsCount += log.size();
		}
		
		return eventsCount;
	}

	/**
	 * Method to reset the number of events added to the store.
	 */
	@ScheduledMethod(start = 0, interval = 1)
	public void resetNewUpdatesCount() {
		this.newUpdatesCount = 0;
	}
	
	/**
	 * Method to retrieve the number of events added to the store in the current tick.
	 *
	 * @return: the number of events added to the store in the current tick.
	 */
	public int getNewUpdatesCount() {
		return this.newUpdatesCount;
	}
}
