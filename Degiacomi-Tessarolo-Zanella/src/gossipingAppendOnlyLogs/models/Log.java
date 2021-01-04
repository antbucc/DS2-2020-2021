package gossipingAppendOnlyLogs.models;

import gossipingAppendOnlyLogs.events.Event;

import java.util.ArrayList;
import java.util.List;

public class Log {
	private final List<Event> events = new ArrayList<>();

	public final PersonPublicKey id;

	public Log(PersonPublicKey id) {
		this.id = id;
	}

	public int getHeight() {
		return events.size();
	}

	public int getLast() {
		return events.size() - 1;
	}

	public List<Event> getEvents(int start, int end) {
		var toReturn = new ArrayList<Event>();
		// useful to tolerate start or end equal to -1
		for (int i = 0; i <= end; i++) {
			if (i >= start)
				toReturn.add(events.get(i));
		}
		return toReturn;
	}

	public void update(Event remoteEvent) {
		if (!remoteEvent.getCreatorId().equals(id)) {
			throw new IllegalArgumentException("remoteEvent does not belong to this log");
		}
		if (containsEvent(remoteEvent)) {
			throw new IllegalStateException("this Log already contains the remoteEvent");
		}
		if (!mayBeNextEvent(remoteEvent)) {
			throw new IllegalArgumentException("even is not compatible");
		}
		events.add(remoteEvent);
	}

	private boolean containsEvent(Event a) {
		return events
				.stream()
				.anyMatch(b -> b.hash().equals(a.hash()));
	}


	private boolean mayBeNextEvent(Event e) {
		if (events.isEmpty()) {
			return true;
		}
		var lastEvent = events.get(getLast());
		return lastEvent.hash().equals(e.getPreviousEventHash());
	}

	public void appendEvent(PersonKeys keysOfLogOwner, Event event) {
		var previousHash = events.isEmpty() ? null : events.get(getLast()).hash();
		event.sign(keysOfLogOwner, previousHash);
		events.add(event);
	}
}
