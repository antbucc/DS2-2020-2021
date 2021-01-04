package distributedgroup_project2.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import distributedgroup_project2.PublicKey;
import distributedgroup_project2.application.Event.EventType;

@SuppressWarnings("serial")
public class Store extends HashMap<PublicKey, Log> {

	private final PublicKey ownerPublicKey;

	/**
	 * Constructor of the class Store.
	 *
	 * @param ownerPublicKey: the public key of the owner.
	 */
	public Store(PublicKey ownerPublicKey) {
		this.ownerPublicKey = ownerPublicKey;
	}

	/**
	 * Method to retrieve the public key of the owner.
	 *
	 * @return: the public key of the owner.
	 */
	public PublicKey getOwnerPublicKey() {
		return ownerPublicKey;
	}

	/**
	 * Method to add a new log in the store.
	 *
	 * @param log: the new log to be added.
	 */
	public void add(Log log) {
		this.putIfAbsent(log.getId(), log);
	}

	/**
	 * Method to retrieve all the log IDs in the store.
	 *
	 * @return: all the log IDs in the store.
	 */
	public Set<PublicKey> getIds() {
		return new HashSet<PublicKey>(this.keySet());
	}

	/**
	 * Method to retrieve all the followed friends IDs in a log.
	 *
	 * @param friend: the public key of a friend;
	 * @return: all the followed friends IDs in a log.
	 */
	public Set<PublicKey> getFollowed(PublicKey friend) {
		Set<PublicKey> followed = new HashSet<>();

		if (this.get(friend) != null) {
			// Loop through events in the log of the friend
			for (Event event : this.get(friend)) {
				// Add the friend of the friend if the event is of type FOLLOW
				if (event.getType() == EventType.FOLLOW) {
					followed.add((PublicKey) event.getContent());
				}
				
				// Remove it if the type is UNFOLLOW
				else if (event.getType() == EventType.UNFOLLOW) {
					followed.remove((PublicKey) event.getContent());
				}
			}
		}

		return followed;
	}

	/**
	 * Method to retrieve all the blocked participants IDs in a log.
	 *
	 * @param friend: the public key of a friend;
	 * @return: all the blocked participants IDs in a log.
	 */
	public Set<PublicKey> getBlocked(PublicKey friend) {
		Set<PublicKey> blocked = new HashSet<>();

		if (this.get(friend) != null) {
			// Loop through events in the log of the friend
			for (Event event : this.get(friend)) {
				// Add the participant as blocked if the event is of type BLOCK
				if (event.getType() == EventType.BLOCK) {
					blocked.add((PublicKey) event.getContent());
				}
				
				// Remove it if the type is UNBLOCK
				else if (event.getType() == EventType.UNBLOCK) {
					blocked.remove((PublicKey) event.getContent());
				}
			}
		}

		return blocked;
	}

	/**
	 * Method to retrieve the frontier of the store for the selected IDs.
	 *
	 * @param ids: the IDs to check against;
	 * @return: the frontier of the store for the selected IDs.
	 */
	public Frontier getFrontier(Set<PublicKey> ids) {
		Frontier frontier = new Frontier();
		
		ids.forEach(id -> {
			Log log = this.get(id);
			if (log != null) {
				frontier.put(id, log.peekLast());
			}
		});
		
		return frontier;
	}

	/**
	 * Method to retrieve a list of new events happened after a certain frontier.
	 *
	 * @param frontier: the frontier to check against;
	 * @return: the list of events happened after the frontier.
	 */
	public List<Event> since(Frontier frontier) {
		List<Event> result = new LinkedList<>();

		// Scan through the frontier
		for (Map.Entry<PublicKey, Event> entry : frontier.entrySet()) {
			// Retrieve the log 
			Log log = this.get(entry.getKey());

			if (log != null) {
				Event frontierEvent = entry.getValue();
				
				// Extract all the events that are more recent than the frontier event for this log
				log.forEach(ev -> {
					if (frontierEvent == null || ev.getIndex() > frontierEvent.getIndex()) {
						result.add(ev);
					}
				});
			}
		}

		return result;
	}

	/**
	 * Method to update the logs given a list of events.
	 *
	 * @param events: a list of new events to be added in the logs.
	 */
	public void update(List<Event> events) {
		for (Log log : this.values()) {
			log.update(events);
		}
	}
}
