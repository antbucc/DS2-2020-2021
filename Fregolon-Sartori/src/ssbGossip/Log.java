package ssbGossip;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


public class Log implements Iterable<Event> {
	PublicKey id;
	SortedSet<Event> events;

	public Log(PublicKey id) {
		this.id = id;
		this.events = new TreeSet<>();
	}

	public void add(Event e) {
		if (!e.getId().equals(this.id))
			System.err.println("Event ID (" + e.getId() + ") does not match log ID (" + this.id + "");
		this.events.add(e);
	}

	public Event getLatest() {
		return this.events.isEmpty() ? null : this.events.last();
	}

	/*
	 * Get all events happened later than "index"
	 */
	public List<Event> getSince(Integer index) {
		ArrayList<Event> result = new ArrayList<>();

		for (Event e : this.events)
			if (e.getIndex() > index)
				result.add(e);

		return result;
	}

	public List<PublicKey> getFollowed() {
		List<PublicKey> ids = new ArrayList<>();

		for (Event e : this.events) {
			if (!(e.getContent() instanceof InterestOperation))
				continue;

			InterestOperation op = (InterestOperation) e.getContent();
			switch (op.operation) {
			case FOLLOW:
				ids.add(op.targetID);
				break;
			case UNFOLLOW:
				ids.remove(op.targetID);
				break;
			default:
				break;
			}
		}

		return ids;
	}

	public List<PublicKey> getBlocked() {
		List<PublicKey> ids = new ArrayList<>();

		for (Event e : this.events) {
			if (!(e.getContent() instanceof InterestOperation))
				continue;

			InterestOperation op = (InterestOperation) e.getContent();
			switch (op.operation) {
			case BLOCK:
				ids.add(op.targetID);
				break;
			case UNBLOCK:
				ids.remove(op.targetID);
				break;
			default:
				break;
			}
		}

		return ids;
	}

	@Override
	public Iterator<Event> iterator() {
		return this.events.iterator();
	}
}
