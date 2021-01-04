package theL0nePr0grammer_2ndAssignment.Logs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import theL0nePr0grammer_2ndAssignment.Relays.Relay;

public class Store {
	private Map<Relay, Log> logset;
	
	public Store() {
		logset = new HashMap<Relay,Log>();
	}
	
	public Store(Store copy) {
		logset = new HashMap<Relay,Log>();
		this.update(copy);
	}
	
	public boolean addKey(Relay r) {
		if (!logset.containsKey(r)) {
			logset.put(r, new Log(r));
			return true;
		}
		return false;
	}
	
	public boolean containsKey(Relay r) {
		return logset.containsKey(r);
	}
	
	public void removeKey(Relay r) {
		logset.remove(r);
	}
	
	public List<Relay> getKeys() {
		List<Relay> toRtn = new ArrayList<Relay>();
		
		logset.forEach((r,l) -> {
			toRtn.add(r);
		});
		
		return toRtn;
	}
	
	public Log getLog(Relay r) {
		if (logset.containsKey(r)) {
			return logset.get(r);
		}
		return null;
	}
	
	public int update(Store s) {
		int count = 0;
		
		for (Relay k : s.getKeys()) {
			this.addKey(k);
			count += this.getLog(k).update(s.getLog(k));
		}
		
		return count;
	}
}
