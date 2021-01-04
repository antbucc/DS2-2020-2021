package ssb_broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents logs stored locally
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class Store {
	private final Map<String, Log> logs;
	private final String id;
	
	public Store(String id) {
		this.id = id;
		this.logs = new HashMap<>();
		this.logs.put(this.id, new Log(this.id));
	}

	// store.add
	public void add(Log log) {
		if (logs.containsKey(log.getId())) {
			return;
		}
		
		this.logs.put(log.getId(), log);
	}
	
	// store.remove
	public void remove(String id) {
		this.logs.remove(id);
	}
	
	// store.get
	public Log getLog(String id) {
		return this.logs.get(id);
	}
	
	// store.ids
	public List<String> getIds() {
		return new ArrayList<>(this.logs.keySet());
	}
	
	// store.frontier
	public Map<String, Event> getFrontier(List<String> ids) {
		Map<String, Event> frontier = new HashMap<>();
		
		for (String id : ids) {
			int lastEventIndex = this.logs.get(id).getLastEventIndex();
			if (lastEventIndex != -1) {
				Event lastEvent = this.logs.get(id).getEvent(lastEventIndex);
				
				frontier.put(id, lastEvent);
			}
		}
		
		return frontier;
	}
	
	// store.frontier
	public Map<String, Event> getFrontier() {
		return getFrontier(this.getIds());
	}
	
	// store.since
	public Map<String, List<Event>> getSince(Map<String, Event> frontier) {
		Map<String, List<Event>> storeSince = new HashMap<>();
		
		for (String key : logs.keySet()) {
			Log log = logs.get(key);
			
			if (frontier.containsKey(key)) {
				Event eventAtFrontier = frontier.get(key);
				
				List<Event> eventsSince = log.getBetween(eventAtFrontier.getIndex(), log.getLastEventIndex());			
				storeSince.put(key, eventsSince);
			}
			else {
				storeSince.put(key, log.getBetween(0, log.getLastEventIndex()));
			}
		}
		
		return storeSince;
	}
	
	// store.update
	public int update(Map<String, List<Event>> news) {
		int updates = 0;
		
		for (String key: news.keySet()) {
			if (!this.logs.containsKey(key)) {
				// We do not add logs to the store.
				// This is done by the open/transitive gossip
				// protocol
				
				continue;
			}
			
			List<Event> events = news.get(key);
			updates += this.logs.get(key).update(events);
		}
		
		return updates;
	}
	
	public List<String> getFollowed(String id) {
		// Get observers that I follow
		List<String> followedByMe = this.logs.get(id).getFollowed();
		
		Set<String> friends = new HashSet<>(followedByMe);
		
		for (String friendId: followedByMe) {
			if (!this.logs.containsKey(friendId)) {
				continue;
			}
			
			List<String> friendsOfFriend = this.logs.get(friendId).getFollowed();
			friends.addAll(friendsOfFriend);
		}
		
		return new ArrayList<>(friends);
	}
	
	public List<String> getBlocked(String id) {
		List<String> followedByMe = this.logs.get(id).getFollowed();
		List<String> blockedByMe = this.logs.get(id).getBlocked();
		
		Set<String> blocked = new HashSet<>();
		
		// 1. Append to blocked set every observer blocked by a friend
		for (String friendId: followedByMe) {
			if (!this.logs.containsKey(friendId)) {
				continue;
			}
			
			List<String> blockedByFriend = this.logs.get(friendId).getBlocked();
			
			blocked.addAll(blockedByFriend);
		}
		
		// 2. Remove from blocked set observers that are followed by a friend
		// This remove observers that are blocked by someone and followed by others
		for (String friendId: followedByMe) {
			if (!this.logs.containsKey(friendId)) {
				continue;
			}
			
			List<String> followedByFriend = this.logs.get(friendId).getFollowed();
			
			blocked.removeAll(followedByFriend);
		}
		
		// 3. Add to blocked set all observers blocked by me
		blocked.addAll(blockedByMe);
		
		// 4. Remove from blocked set all observers that I follow
		blocked.removeAll(followedByMe);
		
		return new ArrayList<>(blocked);
	}
	
	public Store clone() {
		Store cloneStore = new Store(this.id);
		
		for (String id : this.logs.keySet()) {
			cloneStore.logs.put(id, this.logs.get(id).clone());
		}
		
		return cloneStore;
	}
}
