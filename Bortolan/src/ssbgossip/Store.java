package ssbgossip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Store extends HashMap<String, EventLog> {

	public Store() {
		super();
	}

	public void add(EventLog log) {
		super.putIfAbsent(log.id, log);
	}

	public void remove(String id) {
		super.remove(id);
	}

	public EventLog get(String id) {
		return super.get(id);
	}

	public Set<String> getIds() {
		return this.keySet();
	}

	public Set<SimpleEntry<String, Integer>> getFrontier(Set<String> ids) {
		Set<SimpleEntry<String, Integer>> frontier = new HashSet<>();
		ids.forEach((id) -> {
			SimpleEntry<String, Integer> p = new SimpleEntry<>(id, this.get(id).last);
			frontier.add(p);
		});
		return frontier;
	}

	public Map<String, List<Event>> since(Set<SimpleEntry<String, Integer>> frontier) {
		Map<String, List<Event>> events = new HashMap<>();
		frontier.forEach((p) -> {
			List<Event> e = events.getOrDefault(p.getKey(), new ArrayList<>());
			events.putIfAbsent(p.getKey(), e);
			if (this.get(p.getKey()) != null) {
				int last = this.get(p.getKey()).last;
				e.addAll(this.get(p.getKey()).get(p.getValue(), last));
			}
		});
		return events;
	}

	public void update(Map<String, List<Event>> events) throws Exception {
		Iterator<String> it = events.keySet().iterator();
		while (it.hasNext()) {
			String id = it.next();
			this.get(id).update(events.get(id));
		}
	}

	public Set<String> getFollowed(){
		Set<String> followed = new HashSet<>();
		Iterator<EventLog> it = this.values().iterator();
		while(it.hasNext()){
			EventLog e = it.next();
			followed.addAll(e.getFollowed());
		}
		return followed;
	}
	
	public Map<String, Integer> getBlocked(){
		Map<String, Integer> blocked = new HashMap<>();
		Iterator<EventLog> it = this.values().iterator();
		while(it.hasNext()){
			EventLog e = it.next();
			e.getBlocked().forEach((id)->{
				if(blocked.containsKey(id)) {
					blocked.replace(id, blocked.get(id)+1);
				}else {
					blocked.put(id, 1);
				}
			});
		}
		return blocked;
	}

	public static Set<String> idsDifference(Set<String> idsA, Set<String> idsB) {
		Set<String> remaining = new HashSet<>(idsA);
		remaining.removeAll(idsB);
		return remaining;
	}
	
	public int getTotalEvents(){
		int size = 0;
		Iterator<EventLog> it = this.values().iterator();
		while(it.hasNext()){
			EventLog e = it.next();
			size+= e.size();
		}
		return size;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		this.values().forEach((v) -> {
			s.append(v.toString() + "\n");
		});
		return s.toString();
	}
}
