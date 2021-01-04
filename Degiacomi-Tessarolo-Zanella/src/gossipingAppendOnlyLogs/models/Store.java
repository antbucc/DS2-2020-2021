package gossipingAppendOnlyLogs.models;

import gossipingAppendOnlyLogs.events.*;

import java.util.*;

public class Store {

	private Map<PersonPublicKey, Log> logs = new HashMap<>();

	public void add(Log log) {
		if (logs.containsKey(log.id)) {
			throw new IllegalStateException("The log is already present in this store!");
		}
		logs.put(log.id, log);
	}

	public void remove(PersonPublicKey id) {
		logs.remove(id);
	}

	public Log get(PersonPublicKey id) {
		return logs.get(id);
	}

	public Set<PersonPublicKey> getIds() {
		return new HashSet<>(logs.keySet());
	}

	/**
	 * get the current frontier of the store only for the specified ids (table 2 of paper)
	 * @param ids ids to include in the frontier
	 * @return
	 */
	public Frontier getFrontier(Set<PersonPublicKey> ids) {
		var frontier = new Frontier();
		logs.entrySet()
				.stream()
				.filter(entry -> ids.contains(entry.getKey()))
				.map(entry -> new FrontierItem(entry.getKey(), entry.getValue().getLast()))
				.forEach(frontier::addFrontierItem);
		return frontier;
	}

	public List<Event> getEventsSince(Frontier frontier) {
		var events = new ArrayList<Event>();
		for (var item : frontier) {
			var log = logs.get(item.id);
			if(log != null) {
				var lastIndex = log.getLast();

				if (lastIndex > item.last)
					events.addAll(log.getEvents(item.last + 1, lastIndex));
			}
		}
		return events;
	}

	public void update(List<Event> remoteEvents) {
		for (var e : remoteEvents) {
			var log = logs.get(e.getCreatorId());
			if (log == null) {
				throw new IllegalStateException("updates list contains an event belonging to an unknown log");
			}
			log.update(e);
		}
	}

	public Set<PersonPublicKey> getLogsFollowedBy(PersonPublicKey id) {
		var log = logs.get(id);
		var events = log.getEvents(0, log.getLast());
		var followed = new HashSet<PersonPublicKey>();
		for (var event : events) {
			if (event instanceof FollowEvent) {
				followed.add(((FollowEvent) event).followedPerson);
			} else if (event instanceof UnfollowEvent) {
				followed.remove(((UnfollowEvent) event).unfollowedPerson);
			}
		}
		return followed;
	}

	public Set<PersonPublicKey> getLogsBlockedBy(PersonPublicKey id) {
		var log = logs.get(id);
		var events = log.getEvents(0, log.getLast());
		var blocked = new HashSet<PersonPublicKey>();
		for (var event : events) {
			if (event instanceof BlockEvent) {
				blocked.add(((BlockEvent) event).blockedPerson);
			} else if (event instanceof UnblockEvent) {
				blocked.remove(((UnblockEvent) event).unblockedPerson);
			}
		}
		return blocked;
	}
}
