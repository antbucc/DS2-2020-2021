package ssbGossip;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Store {
	HashMap<PublicKey, Log> logs;

	public Store() {
		this.logs = new HashMap<>();
	}

	public void addLog(Log l) {
		this.logs.put(l.id, l);
	}

	public Log removeLog(PublicKey id) {
		return this.logs.remove(id);
	}

	public Log getLog(PublicKey id) {
		return this.logs.get(id);
	}

	public List<PublicKey> getIDs() {
		Set<PublicKey> keys = this.logs.keySet();
		return new ArrayList<>(keys);
	}

	public void update(List<Event> events) {
		for (Event e : events)
			this.update(e);
	}

	public void update(Event event) {
		if (logs.get(event.getId()) == null)
			this.addLog(new Log(event.getId()));
		this.logs.get(event.getId()).add(event);
	}

	public void synchronizeWith(Store s2) {
		Frontier f1 = this.getFrontier(this.getIDs()), f2 = s2.getFrontier(s2.getIDs());
		List<Event> news1 = this.getEventsSince(f1), news2 = s2.getEventsSince(f2);
		this.update(news2);
		s2.update(news1);
	}

	public Frontier getFrontier(List<PublicKey> IDs) {
		Frontier f = new Frontier();

		for (PublicKey id : this.logs.keySet())
			f.add(id, this.logs.get(id).getLatest() != null ? this.logs.get(id).getLatest().getIndex() : 0);

		return f;
	}

	public List<Event> getEventsSince(Frontier f) {
		List<Event> events = new ArrayList<>();
		Log log;
		List<Event> log_since;

		for (FrontierTuple t : f) {
			if ((log = this.logs.get(t.getId())) == null)
				continue;
			if ((log_since = log.getSince(t.getIndex())) == null)
				continue;
			for (Event e : log_since)
				events.add(e);
		}

		return events;
	}

	/*
	 * Recursively search the follow tree and return the set of nodes within N hops
	 */
	public Set<PublicKey> getTransitivelyFollowedBy(PublicKey id, int hops) {
		Set<PublicKey> ids = new HashSet<>();

		if (this.logs.get(id) == null)
			return ids;

		if (hops == 1) {
			ids = new HashSet<>(this.logs.get(id).getFollowed());
		} else {
			for (PublicKey pk1 : this.logs.get(id).getFollowed()) {
				ids.add(pk1);
				for (PublicKey pk2 : this.getTransitivelyFollowedBy(pk1, hops - 1))
					ids.add(pk2);
			}
		}

		return ids;
	}

	/*
	 * Recursively search the blocking tree and return the set of nodes within N
	 * hops
	 */
	public Set<PublicKey> getTransitivelyBlockedBy(PublicKey id, int hops) {
		Set<PublicKey> ids = new HashSet<>();

		if (this.logs.get(id) == null)
			return ids;

		if (hops == 1) {
			ids = new HashSet<>(this.logs.get(id).getBlocked());
		} else {
			for (PublicKey pk1 : this.logs.get(id).getBlocked()) {
				ids.add(pk1);
				for (PublicKey pk2 : this.getTransitivelyBlockedBy(pk1, hops - 1))
					ids.add(pk2);
			}
		}

		return ids;
	}
}
