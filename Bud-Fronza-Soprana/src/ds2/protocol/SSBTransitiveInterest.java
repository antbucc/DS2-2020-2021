package ds2.protocol;

import java.awt.Color;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import bsh.This;
import ds2.application.ApplicationData;
import ds2.application.ApplicationEvent;
import ds2.application.RealApplication;
import ds2.application.UserActions;
import ds2.network.NetworkMessage;
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.nodes.events.TriggerUpdate;
import ds2.protocol.ssb.Identity;
import ds2.protocol.ssb.ProtocolMessage;
import ds2.protocol.ssb.SSBEvent;
import ds2.protocol.ssb.SSBLog;
import ds2.protocol.ssb.Store;
import ds2.protocol.ssb.ProtocolMessage.MessageType;
import ds2.simulator.Oracle;
import ds2.utility.EventHandler;
import ds2.utility.Options;
import ds2.utility.EventHandler.LocalEventHandler;
import ds2.utility.EventHandler.NetworkMessageHandler;
import ds2.utility.logging.Logger;
import ds2.visualization.DisplayManager;

public class SSBTransitiveInterest extends Protocol<RealApplication, Machine<SSBTransitiveInterest, ?>> {

	/**
	 * The identity to which this instance of the protocol is connected. 
	 */
	private Identity myIdentity;
	
	/**
	 * The own store of the protocol, stored locally.
	 */
	private Store myStore;
	
	/**
	 * HashMap of current connections initialized by this protocol, for stateful maintainance
	 * of concurrent connections.
	 */
	private HashMap<Pair<Address, PublicKey>, ArrayList<ProtocolMessage<?>>> initializedByMe;
	
	/**
	 * HashMap of current connections in which this instance is a peer, intialized
	 * by someone else, for stateful maintainance of concurrent connections.
	 * Differentiate the two sets for an appropriate handling of the messages.
	 */
	private HashMap<Pair<Address, PublicKey>, ArrayList<ProtocolMessage<?>>> initializedByOthers;
	
	/**
	 * Followed set with identities indicated by the application. Contains
	 * the friends, first level interest, first hop in the connection graph.
	 */
	private HashSet<PublicKey> followed;
	/**
	 * Blocked set with identities directly blocked by the user connected with this protocol.
	 */
	private HashSet<PublicKey> blocked;
	
	/**
	 * Mapping of my friends and their follow. Contains the second hop in the 
	 * transitive interest graph. 
	 */
	private HashMap<PublicKey, HashSet<PublicKey>> followed2nd;
	
	/**
	 * Mapping of my friends and their blocked identities.
	 */
	private HashMap<PublicKey, HashSet<PublicKey>> blocked2nd;

	
	/**
	 * Constructor for SSBTransitiveInterest protocol
	 * @param m Machine on which the real protocol sits
	 * @param identity Identity used by the protocol
	 */
	public SSBTransitiveInterest(Machine<SSBTransitiveInterest, ?> m, Identity identity) {
		super(m, identity.getPublicKey());
		this.myIdentity = identity;
		this.myStore = new Store(identity, this.getAddress());
		this.initializedByMe = new HashMap<>(); // should actually contain only one connection
		this.initializedByOthers = new HashMap<>();
		followed = new HashSet<>();
		blocked = new HashSet<>();
		followed2nd = new HashMap<>();
		blocked2nd = new HashMap<>();
	}

	/**
	 * Returns the list of followed ids, to check when updating and
	 * in the application when following someone.
	 * @return
	 */
	public HashSet<PublicKey> getFollowed() {
		return followed;
	}

	public void setFollowed(HashSet<PublicKey> followed) {
		this.followed = followed;
	}

	/**
	 * Returns the list of blocked ids, to check when updating and in the application when 
	 * doing some user actions.
	 * @return
	 */
	public HashSet<PublicKey> getBlocked() {
		return blocked;
	}

	public void setBlocked(HashSet<PublicKey> blocked) {
		this.blocked = blocked;
	}

	/**
	 * Function that automatically sends any applicationEvent up (towards the application)
	 * @param ev The event to send upwards
	 */
	@EventHandler.ApplicationEventHandler(cls=ApplicationEvent.class)
	public void handleAppEvent(ApplicationEvent ev) {
		// Forward to application
		this.upcall(ev);
	}
	
	/**
	 * Function to handle a TriggerUpdate event. It will restart the trigger and execute
	 * the update automatically at the interval specified in Options.
	 * Implementation of Algorithm 3 up to the update step.
	 * @param ev The event to handle
	 */
	@LocalEventHandler(cls = TriggerUpdate.class)
	public void handleTriggerUpdate(TriggerUpdate ev) { 
		// get peer
		Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
		// Initialize a connection with someone that is not connected with me already
	
		// check that id with which I update is not blocked
		while (addressAndPort.getKey().equals(this.getAddress()) && addressAndPort.getValue().equals(this.getPort()) 
				&& this.initializedByOthers.containsKey(addressAndPort)) {
			addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
		}
		
		ArrayList<ProtocolMessage<?>> exchanged = new ArrayList<>();
		this.prepareStore();		
		this.prepareFor2nd();
		
		// Initialise connection with first message, the frontier
		this.sendFrontierTo(addressAndPort, exchanged);
		
		this.initializedByMe.put(addressAndPort, exchanged);
				
		// Reschedule the same triggerUpdate for UPDATE_INTERVAL seconds in the future
		this.scheduleLocalEvent(Oracle.getInstance().getCurrentTimestamp() + Options.UPDATE_INTERVAL, ev);
	}
	
	/**
	 * Method to prepare the store upon a connection. Rules: 
	 * -> replicate followed identities, found in 'followed' set. If there are some followed
	 *  identities that don't have an entry in the store yet, we create the entry.
	 * -> block and don't replicate identities that are in the 'blocked' set.  
	 *  If there are some blocked identities that are still in the store, we remove them.
	 */
	public void prepareStore() {
		// TODO check transitively followed
		// If the above app has decided to follow someone, prepare the log for them
		HashSet<PublicKey> storeIds = this.myStore.getIds();
		for (PublicKey entry : this.followed) {
			if (!storeIds.contains(entry)) {
				this.myStore.addLog(new SSBLog(entry));
			}
		}
				
		// Check BLOCKED identities, remove their logs locally
		for (PublicKey entry: this.blocked) {
			if (storeIds.contains(entry) && !entry.equals(this.myIdentity.getPublicKey())) {
				this.myStore.removeLogFromStore(entry);
			}
		}
	}

	/**
	 * Upon a connection, also prepare the store for 2nd level interest entities. 
	 * The replication of third parties has the following rules: 
	 *  -> replicate logs of ids that are followed by [at least one of] your friends,
	 *  interest that was known after an update where this info arrived to this store 
	 *  (the replication happens in update steps which come after acquiring this knowledge)
	 *  -> don't replicate logs that are blocked by a friend; replicate identities that were blocked 
	 *  by someone if they are followed by someone (me or in the transitive follow)
	 */
	public void prepareFor2nd() {
		
		HashSet<PublicKey> storeIds = this.myStore.getIds();
		
		// TRANSITIVE FOLLOW 
		
		// replicate ids followed by at least one of my friends
		HashSet<PublicKey> transitivelyFollowed = new HashSet<>();
		for (Map.Entry<PublicKey, HashSet<PublicKey>> followedById : this.followed2nd.entrySet()) {
			transitivelyFollowed.addAll(followedById.getValue());
		}
		// check what's left
		transitivelyFollowed.removeAll(storeIds);
		// Don't replicate logs that I have explicitly blocked, replicate everything else
		transitivelyFollowed.removeAll(this.blocked);
		for (PublicKey newEntry : transitivelyFollowed) {
				this.myStore.addLog(new SSBLog(newEntry));
		}
		
		// TRANSITIVE BLOCK
		
		// get an overview of all the identities blocked by my friends
		HashSet<PublicKey> transitivelyBlocked = new HashSet<>();
		for (Map.Entry<PublicKey, HashSet<PublicKey>> blockedById : this.blocked2nd.entrySet()) {
			transitivelyBlocked.addAll(blockedById.getValue ());
		}
		// block only those that neither me or my friends follow
		transitivelyBlocked.removeAll(this.followed);
		transitivelyBlocked.removeAll(transitivelyFollowed);
		transitivelyBlocked.remove(this.myIdentity.getPublicKey());
		for (PublicKey blockedBySomeone : transitivelyBlocked) {
		
			if (this.myStore.getIds() .contains(blockedBySomeone)) {
				this.myStore.removeLogFromStore(blockedBySomeone);
			}
			
		}
		
	}
	
	/**
	 * Function to create a local event and append it to the local store. It also checks the 
	 * type of user action, if any, to update the sets of followed and blocked identities.
	 * @param content
	 */
	public void createLocalEvent(ApplicationData<?> content) {
		log("SSBProtocol", "Creating new content");
		
		// Data can be of type ID or UserActions
		Object data = content.getData();
		if (data instanceof UserActions) {
			UserActions castedData = (UserActions) data;
			UserActions.Action action = castedData.getAction();
			PublicKey target = castedData.getTarget();
			switch (action) {
				case FOLLOW:
					this.followed.add(target);
					break;
				case UNFOLLOW:
					this.followed.remove(target);
					break;
				case BLOCK:
					this.blocked.add(target);
					break;
				case UNBLOCK:
					this.blocked.remove(target);
					break;
			}
		}
		
		this.myStore.getLog(myIdentity.getPublicKey()).appendLocal(content, myIdentity.getPrivateKey());
		
		// Show content creation
		DisplayManager.getInstance().drawContent(getMachine());
	}
	
	
	/**
	 * Function to handle a ProtocolMessage received from a peer. It handles the FRONTIER
	 * and NEWS according to the logic of message exchange explained in the paper 
	 * and in the report.
	 */
	@NetworkMessageHandler(dataCls = ProtocolMessage.class)
	public void handleProtocolMessage(NetworkMessage<ProtocolMessage<?>> mess) {
		Pair<Address, PublicKey> addressAndPort = new MutablePair<>(mess.getSource(), mess.getSourcePort());	
		ProtocolMessage<?> content = mess.getData();
		
		// Only handle the connections initialized by me
		if (this.initializedByMe.containsKey(addressAndPort) && !this.initializedByOthers.containsKey(addressAndPort)) {
			switch (content.getType()) {				
				case FRONTIER: {
					this.handleReceivedFrontier(addressAndPort, content.getFrontier());
				}; break;
					
				case NEWS: {
					this.handleNews(addressAndPort, content.getNews());
					this.initializedByMe.remove(addressAndPort);
				}; break;
			}
		} else {
			switch (content.getType()) {
				case FRONTIER: {
					
					// Prepare own store with followed and blocked stuff
					this.prepareStore();
					
					// Handle the connections initialized by others
					ArrayList<ProtocolMessage<?>> exchanged = new ArrayList<>();;
					this.sendFrontierTo(addressAndPort, exchanged);
					this.initializedByOthers.put(addressAndPort, exchanged);
					
					this.handleReceivedFrontier(addressAndPort, content.getFrontier());
					
				}; break;
							
				case NEWS: {
					if (this.initializedByOthers.containsKey(addressAndPort)) {
						this.handleNews(addressAndPort, content.getNews());
						this.initializedByOthers.remove(addressAndPort);
					} else {
						err("SSBProtocol", "Someone unknown is sending me their news");
						//System.err.println("SSBProtocol " + identity.getPublicKey() + ": Someone unknown is sending me their news.");
					}
				}; break;
			}
		}
	}
	
	
	/**
	 * Send to a peer my own frontier.
	 * @param addressAndPort
	 */
	public void sendFrontierTo(Pair<Address, PublicKey> addressAndPort, ArrayList<ProtocolMessage<?>> exchanged) {

		HashMap<PublicKey, Integer> myFrontier = this.myStore.getFrontier(this.myStore.getIds());
		log("SSBTransitiveInterest", "Sending frontier to " + addressAndPort.getKey() + " " 
				+ Logger.formatPort(addressAndPort.getValue()) + ". Length: " + myFrontier.size());
		ProtocolMessage<HashMap<PublicKey, Integer>> message = new ProtocolMessage<>(MessageType.FRONTIER, myFrontier);
		
		exchanged.add(message);
		
		this.unicast(addressAndPort.getKey(), addressAndPort.getValue(), message);
	}
	

	/**
	 * Send to a peer the news computed locally, with respect to the frontier that was received. 
	 * @param addressAndPort
	 * @param myNews
	 */
	public void sendNewsTo(Pair<Address, PublicKey> addressAndPort, HashMap<PublicKey, ArrayList<SSBEvent>> myNews) {

		log("SSBTransitiveInterest", "Sending news to " + addressAndPort.getKey() + " " + 
				Logger.formatPort(addressAndPort.getValue()) + ". Length: " + myNews.size());
		ProtocolMessage<HashMap<PublicKey, ArrayList<SSBEvent>>> message = new ProtocolMessage<>(MessageType.NEWS, myNews);
	
		this.unicast(addressAndPort.getKey(), addressAndPort.getValue(), message);
	}
	
	
	/**
	 * Computes the news with respect to the received frontier and sends them to the requesting peer.
	 * @param frontier
	 */
	public void handleReceivedFrontier(Pair<Address, PublicKey> addressAndPort, HashMap<PublicKey, Integer> peerFrontier) {

		log("SSBTransitiveInterest", "Received frontier with " + peerFrontier.size() + " identities.");
		HashMap<PublicKey, ArrayList<SSBEvent>> myNews = this.myStore.getEventsSinceFrontier(peerFrontier);
		this.sendNewsTo(addressAndPort, myNews);
	}
	
	/**
	 * Handles the received news, by updating the local store accordingly.
	 * Implementation is more or less the same as the one of SSBProtocol
	 * @param news
	 */
	public void handleNews(Pair<Address, PublicKey> addressAndPort, HashMap<PublicKey, ArrayList<SSBEvent>> news) {
	
		log("SSBTransitiveInterest", "Received news of " + news.size() + " identities.");
		
		HashMap<PublicKey, ArrayList<SSBEvent>> accept = new HashMap<>();
		ArrayList<ProtocolMessage<?>> exchanged = null;
		HashMap<PublicKey, Integer> exchangedFrontier = null;
		if (this.initializedByMe.containsKey(addressAndPort)) {
			exchanged = this.initializedByMe.get(addressAndPort);
		} else if (this.initializedByOthers.containsKey(addressAndPort)) {
			exchanged = this.initializedByOthers.get(addressAndPort);
		}
		
		// I have exchanged a frontier with this peer
		if (exchanged != null && exchanged.size() == 1) {
			exchangedFrontier = exchanged.get(0).getFrontier();
		}
		
		// Only accept news that I have explicitly required --> against attacks or random replication
		if (exchangedFrontier != null) {
			for (Map.Entry<PublicKey, Integer> entry : exchangedFrontier.entrySet()) {
				PublicKey id = entry.getKey();
				if (news.containsKey(id)) {
					// Check whether the received event is actually the expected one
					if (news.get(id).get(0).getIndex() == entry.getValue() +1) {
						accept.put(id, news.get(id));
					}
				}
			}
		}		

		log("SSBTransitive", "Accepting news of " + accept.size() + " identities.");
		
		this.myStore.update(accept);
		this.handleNewerThan(exchangedFrontier);
		
	}

	/**
	 * Method to handle the user actions of other peers, in order to update
	 * the transitive followed and blocked sets accordingly. 
	 * @param peer, the Identity for which I am saving information in the HashMap 
	 * @param message, the user action received and checked before sending it to the application. 
	 * TODO should we send it to the application?
	 */
	// TODO Check which stores to replicate at run time during the prepare store of the update; 
	// because here I just save the information that needs to be saved about the peers
	private void handleUserActions(PublicKey peer, UserActions message) {
		
		UserActions.Action action = message.getAction();
		PublicKey target = message.getTarget();
		
		switch (action) {
			case FOLLOW:
				// if a friend follows someone add it to the set of followed2nd (transitive follow).
				// NOTE: this target may also be someone which I follow or I block
				// (Check at runtime and replicate it even if I stop following it)
				
				if (this.followed2nd.get(peer).add(target)) {
					log("SSBTransitiveInterest", "New FOLLOW received from peer: ("
							+ Logger.formatPort(peer) + " -> " + Logger.formatPort(target) + ")" );
				} else {
					log("SSBTransitiveInterest", "Old FOLLOW received from peer: ("
							+ Logger.formatPort(peer) + " -> " + Logger.formatPort(target) + ")" );
				}
				
				break;
			case UNFOLLOW: 
				if (this.followed2nd.get(peer).contains(target)){
					this.followed2nd.remove(target);

					log("SSBTransitiveInterest", "UNFOLLOW received from peer: ("
							+ Logger.formatPort(peer) + " -x-> " + Logger.formatPort(target) + ")" );
				}
				break;
			case BLOCK:

				if (this.blocked2nd.get(peer).add(target)) {
					log("SSBTransitiveInterest", "New BLOCK received from peer: ("
							+ Logger.formatPort(peer) + " -> " + Logger.formatPort(target) + ")" );
				} else {
					log("SSBTransitiveInterest", "Old BLOCK received from peer: ("
							+ Logger.formatPort(peer) + " -> " + Logger.formatPort(target) + ")" );
				}
				
				break;
			case UNBLOCK: 
				
				if (this.blocked2nd.get(peer).contains(target)){
					this.blocked2nd.remove(target);

					log("SSBTransitiveInterest", "UNBLOCK received from peer: ("
							+ Logger.formatPort(peer) + " -> " + Logger.formatPort(target) + ")" );
				}
				
				break;
		}
		
	}
	
	
	/**
	 * Locally compute the newer messages that have to be handled and sent up to the application 
	 * because they are newer than those requested with the frontier. 
	 * @param the list of accepted news, explicitly required
	 * @param exchangedFrontier
	 */
	private void handleNewerThan(HashMap<PublicKey, Integer> exchangedFrontier) {
		// Get the news with respect to exchanged frontier
		HashMap<PublicKey, ArrayList<SSBEvent>> news = this.myStore.getEventsSinceFrontier(exchangedFrontier);
		
		for (Map.Entry<PublicKey, ArrayList<SSBEvent>> list : news.entrySet()) {
			PublicKey peer = list.getKey();
			// ignore my messages and those of peers only in transitive follow
			if (peer != this.myIdentity.getPublicKey() || 
					(!this.followed.contains(peer) && this.followed2nd.containsKey(peer)) ) {
				
				// initialize empty lists for the follow and blocked of my peers upon first messages
				// from them
				if (! this.followed2nd.containsKey(peer)) {
					HashSet<PublicKey> followedByPeer = new HashSet<>();
					this.followed2nd.put(peer, followedByPeer);
				}
				if (! this.blocked2nd.containsKey(peer)) {
					HashSet<PublicKey> blockedByPeer = new HashSet<>();
					this.blocked2nd.put(peer, blockedByPeer);
				}
				
				for (SSBEvent newEvent : list.getValue()) {
					
					ApplicationData<?> content = newEvent.getContent();
					
					// handle the new follow and block events of my friends
					if (content.getData() instanceof UserActions) {
						this.handleUserActions(peer, (UserActions) content.getData());
					}					
					
					// send to app
					this.upcall(newEvent.getContent());
					
				}
			}
		}
				
	}
	
	
	/**
	 * Getter for Identity
	 * @return the identity of the protocol (private + public key)
	 */
	public Identity getIdentity() {
		return myIdentity;
	}
}
