package ds2.protocol;

//Standard libraries
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Apache commons
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

// Custom libraries
import ds2.application.ApplicationData;
import ds2.application.ApplicationEvent;
import ds2.application.RealApplication;
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
import ds2.utility.logging.Logger;
import ds2.visualization.DisplayManager;

/**
 * This class represents the SSB protocol
 */
public class SSBProtocol extends Protocol<RealApplication, Machine<SSBProtocol, ?>> {
	
	private Identity identity;
	private Store myStore;
	// concurrent connections: differentiate for handling the messages accordingly
	private HashMap<Pair<Address, PublicKey>, ArrayList<ProtocolMessage<?>>> initializedByMe;
	private HashMap<Pair<Address, PublicKey>, ArrayList<ProtocolMessage<?>>> initializedByOthers;
	
	/**
	 * Constructor for SSBProtocol
	 * @param m Machine on which the real protocol sits
	 * @param identity Identity used by the protocol
	 */
	public SSBProtocol(Machine<SSBProtocol, ?> m, Identity identity) {
		super(m, identity.getPublicKey());
		this.identity = identity;
		this.myStore = new Store(identity, this.getAddress());
		this.initializedByMe = new HashMap<>(); // should actually contain only one connection
		this.initializedByOthers = new HashMap<>();
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
	 * Function to handle a TriggerUpdate event. It will restart the trigger and execute the update automatically at the interval specified in Options
	 * For now, implements the OpenGossip algorithm
	 * @param ev The event to handle
	 */
	@LocalEventHandler(cls = TriggerUpdate.class)
	public void handleTriggerUpdate(TriggerUpdate ev) { 
		// get peer
		Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
		// Initialize a connection with someone that is not connected with me already
		while (addressAndPort.getKey().equals(this.getAddress()) && addressAndPort.getValue().equals(this.getPort()) 
				&& this.initializedByOthers.containsKey(addressAndPort)) {
			addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
		}
		ArrayList<ProtocolMessage<?>> exchanged = new ArrayList<>();
		
		// Initialise connection with first message, save message in exchanged list
		this.sendIdsTo(addressAndPort, exchanged);
		

		this.initializedByMe.put(addressAndPort, exchanged);
				
		// Reschedule the same triggerUpdate for UPDATE_INTERVAL seconds in the future
		this.scheduleLocalEvent(Oracle.getInstance().getCurrentTimestamp() + Options.UPDATE_INTERVAL, ev);
	}
	
	/**
	 * Function to handle a NetworkMessage received from a peer.
	 */
	@NetworkMessageHandler(dataCls = ProtocolMessage.class)
	public void handleProtocolMessage(NetworkMessage<ProtocolMessage<?>> mess) {
		Pair<Address, PublicKey> addressAndPort = new MutablePair<>(mess.getSource(), mess.getSourcePort());	
		ProtocolMessage<?> content = mess.getData();
		
		// Only handle the connections initialized by me
		if (this.initializedByMe.containsKey(addressAndPort) && !this.initializedByOthers.containsKey(addressAndPort)) {
			switch (content.getType()) {
				case UPDATE_INIT: {
					this.handleIds(content.getIds());
					
					// if this is the initiator of the connection, I need to send frontier now
					// and to save what I sent
					this.sendFrontierTo(addressAndPort, this.initializedByMe.get(addressAndPort));				
				}; break;
				
				case FRONTIER: {
					this.handleReceivedFrontier(addressAndPort, content.getFrontier()); 
				}; break;
					
				case NEWS: {
					this.handleNews(addressAndPort, content.getNews());
					// TODO add some "close" message?
					this.initializedByMe.remove(addressAndPort);
				}; break;
			}
		} else {
			switch (content.getType()) {
				case UPDATE_INIT: {
					// Handle the connections initialized by others
					ArrayList<ProtocolMessage<?>> exchanged = new ArrayList<>();; 
					this.initializedByOthers.put(addressAndPort, exchanged);

					// If I am not the initiator, I need to send back my ids 
					this.sendIdsTo(addressAndPort, exchanged);
					
					this.handleIds(content.getIds());
					
				}; break;
				
				case FRONTIER: {
					if (this.initializedByOthers.containsKey(addressAndPort)) {
						// If I am not the initiator, I didn't send the frontier yet,
						// so send frontier before handling the received frontier
						this.sendFrontierTo(addressAndPort, this.initializedByOthers.get(addressAndPort)); 
						
						this.handleReceivedFrontier(addressAndPort, content.getFrontier()); 
					} else {
						err("SSBProtocol", "Someone unknown is sending me a frontier");
						//System.err.println("SSBProtocol " + identity.getPublicKey() + ": Someone unknown is sending me a frontier.");
					}
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
	
	public Store getStore() {
		return this.myStore;
	}
	
	/**
	 * Function to create a local event and append it to the local store
	 * @param content
	 */
	public void createLocalEvent(ApplicationData<?> content) {
		log("SSBProtocol", "Creating new content");
		//System.out.println("[SSBProtocol] " + this.getDown().getAddress() +": Creating new content.");
		this.myStore.getLog(identity.getPublicKey()).appendLocal(content, identity.getPrivateKey());
		
		// Show content creation
		DisplayManager.getInstance().drawContent(getMachine());
	}
	
	/**
	 * Send to a peer my ids, so that he can prepare the store for the update step.
	 * @param addressAndPort
	 */
	public void sendIdsTo(Pair<Address, PublicKey> addressAndPort, ArrayList<ProtocolMessage<?>> exchanged) {
		// Send to B own IDs

		Set<PublicKey> myIds = this.myStore.getIds();
		log("SSBProtocol", "Sending ids to " + addressAndPort.getKey() + " " +
								Logger.formatPort(addressAndPort.getValue()) + ". Length: " + myIds.size());
		ProtocolMessage<Set<PublicKey>> message = new ProtocolMessage<>(MessageType.UPDATE_INIT, myIds);
		
		exchanged.add(message);
		
		this.unicast(addressAndPort.getKey(), addressAndPort.getValue(), message);
	}
	
	/**
	 * Send to a peer my own frontier.
	 * @param addressAndPort
	 */
	public void sendFrontierTo(Pair<Address, PublicKey> addressAndPort, ArrayList<ProtocolMessage<?>> exchanged) {
		// Send to B own frontier
		HashMap<PublicKey, Integer> myFrontier = this.myStore.getFrontier(this.myStore.getIds());
		log("SSBProtocol", "Sending frontier to " + addressAndPort.getKey() + " " 
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

		log("SSBProtocol", "Sending news to " + addressAndPort.getKey() + " " + 
				Logger.formatPort(addressAndPort.getValue()) + ". Length: " + myNews.size());
		ProtocolMessage<HashMap<PublicKey, ArrayList<SSBEvent>>> message = new ProtocolMessage<>(MessageType.NEWS, myNews);
		this.unicast(addressAndPort.getKey(), addressAndPort.getValue(), message);
	}
	
	/**
	 * First step of Open Gossip protocol: receive the ids of the peer that I connected with, and 
	 * add missing ids into my store. To be called before the update step.
	 * @param ids store'.ids
	 */
	public void handleIds(HashSet<PublicKey> ids) {

		log("SSBProtocol", "Received ids. Length: " + ids.size());
		Set<PublicKey> myIds = this.myStore.getIds();	// store.ids
		Set<PublicKey> myMissing = new HashSet<>(ids);  // assume I don't have any of them
		myMissing.removeAll(myIds);			// store'.ids - store.ids --> remove the ids that I have already

		// Create new logs locally
		for (PublicKey id: myMissing) {
			this.myStore.addLog(new SSBLog(id)); // last=-1
		}		
	}
	
	/**
	 * Computes the news with respect to the received frontier and sends them to the requesting peer.
	 * @param frontier
	 */
	public void handleReceivedFrontier(Pair<Address, PublicKey> addressAndPort, HashMap<PublicKey, Integer> peerFrontier) {

		log("SSBProtocol",  "Received frontier with " + peerFrontier.size() + " identities.");
		HashMap<PublicKey, ArrayList<SSBEvent>> myNews = this.myStore.getEventsSinceFrontier(peerFrontier);
		this.sendNewsTo(addressAndPort, myNews);
		
	}
	
	/**
	 * Handles the received news, by updating the local store accordingly.
	 * @param news
	 */
	public void handleNews(Pair<Address, PublicKey> addressAndPort, HashMap<PublicKey, ArrayList<SSBEvent>> news) {
		
		log("SSBProtocol", "Received news of " + news.size() + " identities.");
		
		HashMap<PublicKey, ArrayList<SSBEvent>> accept = new HashMap<>();
		ArrayList<ProtocolMessage<?>> exchanged = null;
		HashMap<PublicKey, Integer> exchangedFrontier = null;
		if (this.initializedByMe.containsKey(addressAndPort)) {
			exchanged = this.initializedByMe.get(addressAndPort);
		} else if (this.initializedByOthers.containsKey(addressAndPort)) {
			exchanged = this.initializedByOthers.get(addressAndPort);
		}
		
		// I have exchanged ids and news with this peer
		if (exchanged != null && exchanged.size() == 2) {
			exchangedFrontier = exchanged.get(1).getFrontier();
		}
		
		// Only accept news that I have explicitly required
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

		log("SSBProtocol", "Accepting news of " + accept.size() + " identities.");
		
		this.myStore.update(accept);
		this.sendToAppNewerThan(exchangedFrontier);
		
	}
	
	/**
	 * Locally compute the newer messages that have to be sent up to the application 
	 * because they are newer than those requested with the frontier. 
	 * @param the list of accepted news, explicitly required
	 * @param exchangedFrontier
	 */
	private void sendToAppNewerThan(HashMap<PublicKey, Integer> exchangedFrontier) {
		
		// Get the news with respect to exchanged frontier
		HashMap<PublicKey, ArrayList<SSBEvent>> news = this.myStore.getEventsSinceFrontier(exchangedFrontier);
		
		for (Map.Entry<PublicKey, ArrayList<SSBEvent>> list : news.entrySet()) {
			if (list.getKey() != this.identity.getPublicKey()) {
				// ignore my messages
				for (SSBEvent newEvent : list.getValue()) {
					
					this.upcall(newEvent.getContent());
					
				}
			}
		}
		
	}

	/**
	 * Just a function to have the reference of the steps of the protocol from a global perspective.
	 * @param peer
	 */
	private void connectWithPeer(SSBProtocol peer) {
		// Get list of own ids and of the ids of the peer
		Set<PublicKey> myIds = myStore.getIds();	//store.ids
		Set<PublicKey> peerIds = peer.getStore().getIds(); //store'.ids
	
		// Copy everything into a new set, to do difference
		Set<PublicKey> peerMissing = new HashSet<>(myStore.getIds());  // assume peer does not have any of my logs
		peerMissing.removeAll(peerIds); //store.ids - store'.ids Remove the ones that it has already
		
		Set<PublicKey> myMissing = new HashSet<>(peer.getStore().getIds());
		myMissing.removeAll(myIds);	// store'.ids - store.ids
		
		// Create new logs locally
		for (PublicKey id: myMissing) {
			this.myStore.addLog(new SSBLog(id));
		}
		
		// Create new logs remotely
		for (PublicKey id: peerMissing) {
			peer.getStore().addLog(new SSBLog(id));
		}
		
		// Update store and store'; store = myStore; store' = peer
		HashMap<PublicKey, Integer> myFrontier = myStore.getFrontier(myStore.getIds());
		HashMap<PublicKey, Integer> peerFrontier = peer.getStore().getFrontier(peer.getStore().getIds());
		
		HashMap<PublicKey, ArrayList<SSBEvent>> myNews = myStore.getEventsSinceFrontier(peerFrontier);
		HashMap<PublicKey, ArrayList<SSBEvent>> peerNews = myStore.getEventsSinceFrontier(myFrontier);
		
		myStore.update(peerNews);
		peer.getStore().update(myNews);
	}
	
	/**
	 * Getter for Identity
	 * @return the identity of the protocol (private + public key)
	 */
	public Identity getIdentity() {
		return identity;
	}
}
