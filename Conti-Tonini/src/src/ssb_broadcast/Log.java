package ssb_broadcast;

import java.util.ArrayList;
import java.util.List;

import ssb_broadcast.Event.EventType;

/**
 * Defines a sequence of events from a
 * predefined id
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class Log {
	// Id of this log
	private final String id;
	
	// Append-only structure
	private final List<Event> events;
	
	// create
	public Log(String id) {
		super();
		this.id = id;
		this.events = new ArrayList<>();
	}

	// log.id
	public String getId() {
		return this.id;
	}
	
	// log.event[i]
	public Event getEvent(int index) {
		return this.events.get(index);
	}
	
	// log.last
	public int getLastEventIndex() {
		return this.events.size() - 1;
	}
	
	// log.append
	public void append(String content, String privateKey) {
		this.append(EventType.MESSAGE, content, privateKey);
	}
	
	// log.follow
	public void follow(String id, String privateKey) {
		this.append(EventType.FOLLOW, id, privateKey);
	}
	
	// log.unfollow
	public void unfollow(String id, String privateKey) {
		this.append(EventType.UNFOLLOW, id, privateKey);
	}
	
	// log.block
	public void block(String id, String privateKey) {
		this.append(EventType.BLOCK, id, privateKey);
	}
	
	// log.unblock
	public void unblock(String id, String privateKey) {
		this.append(EventType.UNBLOCK, id, privateKey);
	}
	
	// log.followed
	public List<String> getFollowed() {
		List<String> followed = new ArrayList<>();
		
		// Checks for FOLLOW/UNFOLLOW events
		// We are guaranteed that every UNFOLLOWed
		// observer is removed because UNFOLLOW(id)
		// are logged after a FOLLOW(id)
		for (Event event: this.events) {			
			// If you followed someone, add
			if (event.getType() == EventType.FOLLOW) {
				followed.add(event.getContent());
			}
			// But if you unfollowed it, remove
			else if ((event.getType() == EventType.UNFOLLOW || event.getType() == EventType.BLOCK) &&
					followed.contains(event.getContent())) {
				followed.remove(event.getContent());
			}
		}
		
		return followed;
	}
	
	// log.blocked
	public List<String> getBlocked() {
		List<String> blocked = new ArrayList<>();
		
		// Checks for BLOCK/UNBLOCK events
		// We are guaranteed that every UNBLOCKed
		// observer is removed because UNBLOCK(id)
		// are logged after a BLOCK(id)
		for (Event event: this.events) {
			// If you blocked someone, add
			if (event.getType() == EventType.BLOCK) {
				blocked.add(event.getContent());
			}
			// But if you unblocked it, remove
			else if ((event.getType() == EventType.UNBLOCK || event.getType() == EventType.FOLLOW) &&
					blocked.contains(event.getContent())) {
				blocked.remove(event.getContent());
			}
		}
		
		return blocked;
	}
	
	// log.update
	public int update(List<Event> events) {
		int updateCount = 0;
		
		for (Event event: events) {
			if (!this.events.contains(event) && event.verify(event.getId())) {
				this.events.add(event);
			
				updateCount++;
			}
		}
		
		return updateCount;
	}
	
	// log.get
	public List<Event> getBetween(int startIndex, int endIndex) {
		List<Event> subset = new ArrayList<>();
		for (int index = startIndex; index <= endIndex; index++) {
			subset.add(this.events.get(index));
		}
		
		return subset;
	}
	
	public Log clone() {
		Log clone = new Log(this.id);
		
		for (Event event: this.events) {
			clone.events.add(event.clone());
		}
		
		return clone;
	}
	
	private void append(EventType type, String content, String privateKey) {
		int lastEventHashCode = 0;
		int lastEventIndex = this.getLastEventIndex();
		if (lastEventIndex != -1) {
			lastEventHashCode = this.events.get(this.getLastEventIndex()).hashCode();
		}
		
		Event event = new Event(type, this.id, lastEventIndex + 1, content, lastEventHashCode);
		event.sign(privateKey);
		
		this.events.add(event);
	}
}
