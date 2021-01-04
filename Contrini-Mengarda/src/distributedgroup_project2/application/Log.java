package distributedgroup_project2.application;

import java.util.LinkedList;
import java.util.List;

import distributedgroup_project2.PrivateKey;
import distributedgroup_project2.PublicKey;
import distributedgroup_project2.Utils;
import distributedgroup_project2.application.Event.EventType;

@SuppressWarnings("serial")
public class Log extends LinkedList<Event> {
	private final PublicKey id;

	/**
	 * Constructor of the class Log.
	 * 
	 * @param id: the public key of the owner of the log.
	 */
	public Log(PublicKey id) {
		this.id = id;
	}

	/**
	 * Method to retrieve the id (public key) of the log.
	 * 
	 * @return: the id of the log.
	 */
	public PublicKey getId() {
		return id;
	}

	/**
	 * Method to create a generic event in the log.
	 * 
	 * @param privateKey: the privateKey of the creator of the event;
	 * @param content: the content of the event.
	 */
	public void append(PrivateKey privateKey, String content) {
		Event event = new Event(privateKey, id, getLastHash(), getLastIndex() + 1, EventType.MESSAGE, content);
		this.add(event);
	}

	/**
	 * Method to create a follow event in the log.
	 * 
	 * @param privateKey: the privateKey of the creator of the event;
	 * @param participant: the participant to follow.
	 */	
	public void follow(PrivateKey privateKey, PublicKey participant) {
		Event event = new Event(privateKey, id, getLastHash(), getLastIndex() + 1, EventType.FOLLOW, participant);
		this.add(event);
	}

	/**
	 * Method to create an unfollow event in the log.
	 * 
	 * @param privateKey: the privateKey of the creator of the event;
	 * @param participant: the participant to unfollow.
	 */	
	public void unfollow(PrivateKey privateKey, PublicKey participant) {
		Event event = new Event(privateKey, id, getLastHash(), getLastIndex() + 1, EventType.UNFOLLOW, participant);
		this.add(event);
	}

	/**
	 * Method to create a block event in the log.
	 * 
	 * @param privateKey: the privateKey of the creator of the event;
	 * @param participant: the participant to block.
	 */		
	public void block(PrivateKey privateKey, PublicKey participant) {
		Event event = new Event(privateKey, id, getLastHash(), getLastIndex() + 1, EventType.BLOCK, participant);
		this.add(event);
	}

	/**
	 * Method to create an unblock event in the log.
	 * 
	 * @param privateKey: the privateKey of the creator of the event;
	 * @param participant: the participant to unblock.
	 */	
	public void unblock(PrivateKey privateKey, PublicKey participant) {
		Event event = new Event(privateKey, id, getLastHash(), getLastIndex() + 1, EventType.UNBLOCK, participant);
		this.add(event);
	}

	/**
	 * Method to update the log with a list of new events.
	 * 
	 * @param incomingEvents: the list of new events to be added in the log.
	 */	
	public void update(List<Event> incomingEvents) {
		incomingEvents.forEach(event -> {
			// Check if the event belongs to this log,
			// and whether the event came after the last one already in the log
			if (event.getCreatorId().equals(this.id) && event.getIndex() > getLastIndex()) {
				// Check if the event is valid
				if (event.verifySignature()) {
					this.add(event);					
				} else {
					Utils.logError("Event signature cannot be verified");
				}
			}
		});
	}

	/**
	 * Method to retrieve the last event index.
	 * 
	 * @return: the index of the last event.
	 */	
	private int getLastIndex() {
		if (this.size() == 0) {
			return 0;
		}

		return this.getLast().getIndex();
	}

	/**
	 * Method to retrieve the hash of the last event.
	 * 
	 * @return: the hash of the last event.
	 */	
	private Integer getLastHash() {
		Event event = this.peekLast();
		if (event != null) {
			return event.hashCode();
		}
		return null;
	}
}
