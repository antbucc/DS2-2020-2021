package ds2.protocol.ssb;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.MutablePair;
import org.eclipse.jdt.annotation.NonNull;

import ds2.nodes.Address;
import ds2.simulator.Oracle;
import ds2.utility.logging.Logger;

/**
 * This class holds the functionalities of the Store in the Secure Scuttlebutt protocol 
 * (section 2.3 of the paper). It implements the functionalities presented in table 2.
 * It contains a set of logs stored locally, which can be updated as a result of a direct user
 * action (follow/block) or by adding an event to a log. 
 * It contains an updated frontier, a set that represents the latest known indexes about logs in a store,
 * therefore the latest known event for a source. It also contains the list of identities. 
 * Frontier format: {(logi.id, logi.last), ...}. The difference between frontiers is used to update the 
 * stores.
 * @author budge
 *
 */
// TODO maybe change way of getting the frontier (now it is updated when adding new elements
// and just queried, but local events circumnavigate it?)

public class Store {

	private Identity ownIdentity;

	private Address address; 
	
	// The complete log of per-source messages and its frontier
	private HashMap<PublicKey, SSBLog> logs; //<id, <id, <id, event>>>
	//private HashMap<PublicKey, Integer> frontier;

	public Store(Identity ownIdentity, Address address) {
		this.ownIdentity = ownIdentity;
		this.logs = new HashMap<>();
		this.address = address;
		//this.frontier = new HashMap<>();
		
		SSBLog ownLog = new SSBLog(this.ownIdentity.getPublicKey());
		this.addLog(ownLog);
	}
	
	/** 
	 * Add a log to the store. (Called when updating the stores ?)
	 * Also update the frontier
	 * store <-- store.add(log).
	 * @param log
	 */
	public void addLog(SSBLog log) {
		this.logs.put(log.getId(), log);
		//this.frontier.put(log.getId(), log.getLastIndex());
	}
	
	/**
	 * Removes a log with given id from the store. 
	 * store <-- store.remove(id)
	 * @param id
	 * @return true if the operation was successful, false if the store did not contain that id.
	 */
	public boolean removeLogFromStore(PublicKey id) {
		if (logs.containsKey(id)) {
			logs.remove(id);
			//frontier.remove(id);
			return true;
		}
		return false;
	}
	
	/**
	 * Get the log with given id from the store. Return null if the id
	 * is not present in the log.
	 * log <-- store.get(id);
	 * @param id
	 */
	public SSBLog getLog(PublicKey id) {
		if (logs.containsKey(id)) {
			return logs.get(id);
		}
		return null;
	}
	
	/**
	 * Get the set of ids of the log in the store. 
	 * ids <-- store.ids
	 * @return a HashSet of PublicKeys, taken from the HashMap used as store. 
	 */
	public  HashSet<PublicKey> getIds(){
		return new  HashSet<>(logs.keySet());
	}
	
	
	/**
	 * Get the current frontier of the store only for the given ids.  
	 * frontier <-- store.frontier(ids)
	 * @param ids
	 * @return a HashMap<PublicKey, Integer> that represents the frontier.
	 */
	public HashMap<PublicKey, Integer> getFrontier(HashSet<PublicKey> ids){
		HashMap<PublicKey, Integer> res = new HashMap<>();
		for (PublicKey id: ids) { 
			if (this.logs.containsKey(id)) {
				int last = this.logs.get(id).getLastIndex();
				res.put(id, last);				
			}
		}
		return res;
	}
	
//	public HashMap<PublicKey, Integer> getFrontier(HashSet<PublicKey> ids){
//	HashMap<PublicKey, Integer> res = new HashMap<>();
//	for (PublicKey id: ids) {
//		if (this.frontier.containsKey(id)){
//			res.put(id, this.frontier.get(id));
//		}
//	}
//	return res;
//}
	
	/**
	 * Get the set of events that happened after frontier. Used in the update step, to send to 
	 * the communicating entity the events that are newer than their frontier. 
	 * events <-- store.since(frontier)
	 * @param frontier of the peer
	 * @return new events in this store that are not yet in the other, independently
	 * on their source. 
	 */
	public HashMap<PublicKey, ArrayList<SSBEvent>> getEventsSinceFrontier(HashMap<PublicKey, Integer> otherFrontier){
		HashMap<PublicKey, ArrayList<SSBEvent>> eventsById = new HashMap<>();
		
		HashMap<PublicKey, Integer> frontier = this.getFrontier(new HashSet<>(otherFrontier.keySet()));
		
		for (Map.Entry<PublicKey, Integer> entry: otherFrontier.entrySet()) {
			
			PublicKey id = entry.getKey();
			if (this.logs.containsKey(id)) {
				// common entries: the other Store wants newer events, so this holds: 
				int start = entry.getValue()+1; //A = (b,5) ; B = (b,9) , C = (c = -1)
				int end = frontier.get(id); // start = 10, end = 5
				// note that if start > end it means that this Store is behind for the log of that id, so no sense of sending anything
				
				if (end >= 0) {
					// take the corresponding elements from the log
					ArrayList<SSBEvent> events = this.logs.get(id).getEvents(start, end);
					if (events != null) {
						eventsById.put(id, events);
					}
				}
				
			}
		}
		return eventsById;
	}
	
	/**
	 * Update the logs in store with given events (received during update?).
	 * store <-- store.update(events)
	 * @param events 
	 */
	// TODO 
	// Should we check for the hash of the previous event? For now, I only check seqn 
	// Is the log already initialised? Yes, because this is done in the preliminary step before updating
	public void update(HashMap<PublicKey, ArrayList<SSBEvent>> eventsById) {
		
		for (Map.Entry<PublicKey, ArrayList<SSBEvent>> entry : eventsById.entrySet()) {
			// Place Events in the corresponding log
			PublicKey id = entry.getKey();
			ArrayList<SSBEvent> list = entry.getValue();
			
			if (this.logs.containsKey(id) && !list.isEmpty()) {
				SSBLog idLog = this.logs.get(id);
				
				SSBEvent newEvent = list.get(0);
				// Check that events are in order, to maintain total order and connectivity
				// One connection: expected events
				if (newEvent.getIndex() == idLog.getLastIndex() + 1) {  
					
					// Verify that the first event of the list was signed by an expected identity
					String hashedEvent = idLog.getHashedEvent(newEvent);
					boolean verification = false;
					try {
						verification = idLog.verifySign(hashedEvent, newEvent.getSignature(), id);
					}
					catch (Exception ex) {
						System.err.println("Event sign verification went wrong");
						ex.printStackTrace();
						System.exit(-1);
					}
					if (verification) {
						idLog.update(list);
						//this.frontier.put(id, idLog.getLastIndex());
						
						for (int i = 0; i < list.size(); i++) {
							this.log("Store", " received news from " + Logger.formatPort(id)+ " :" + list.get(i).getContent());
						} 
					}
				} else {
					// Two concurrent connections: list may have already been (partially) updated
					ArrayList<SSBEvent> newer = new ArrayList<>();
					// Check whether the first viewed event was verified
					boolean verified = false;
					for (int i = 0; i < list.size(); i++) {
						SSBEvent concurrentNewEvent = list.get(i);
						
						if (concurrentNewEvent.getIndex() >= idLog.getLastIndex() + 1) {
							if (!verified) {
								// Verify that the first event of the list was signed by an expected identity
								String hashedEvent = idLog.getHashedEvent(concurrentNewEvent);
								boolean verification = false;
								try {
									verification = idLog.verifySign(hashedEvent, concurrentNewEvent.getSignature(), id);
								}
								catch (Exception ex) {
									System.err.println("Concurrent event sign verification went wrong");
									ex.printStackTrace();
									System.exit(-1);
								}
								if (verification) {
									// Found new events starting from list.get(i)
									newer.add(list.get(i));
									this.log("Store", " received news from " + Logger.formatPort(id)+ " :" + list.get(i).getContent());
									verified = true;
								}
							}
							else {
								// Found new events starting from list.get(i)
								newer.add(list.get(i));
								this.log("Store", " received news from " + Logger.formatPort(id)+ " :" + list.get(i).getContent());
								}
						}
					}
					if (!newer.isEmpty()) {
						idLog.update(newer);
						//this.frontier.put(id, idLog.getLastIndex());
					}
				}
			} else {
				System.err.println("Unknown id");
			}
					
		}
		
	}
	
	/**
	 * Log something for this protoco.l using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, @NonNull String msg) {
		this.log(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}
	
	/**
	 * Log something for this store using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, double timestamp, @NonNull String msg) {
		Oracle.getInstance().getLogger().getMachineLogger(this.address).print(System.out, msg, tag, ""+timestamp, this.address.toString(), ""+Logger.formatPort(this.ownIdentity.getPublicKey()));
	}

	
}
