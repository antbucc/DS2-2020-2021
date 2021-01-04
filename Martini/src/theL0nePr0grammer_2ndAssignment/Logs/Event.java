package theL0nePr0grammer_2ndAssignment.Logs;

import java.util.Objects;
import theL0nePr0grammer_2ndAssignment.Relays.Relay;

public class Event {
	private Relay id;
	private String previous; //The md5 computed hash;
	private int index;
	private Object content;
	private String signature;
	
	public Event(Relay id, String previous, int index, Object content) {
		this.id = id;
		this.previous = previous;
		this.index = index;
		this.content = content;
		this.signature = "";
	}
	
	//STANDARD METHOD OVERRIDE
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event cmp = (Event) o;
        return this.id == cmp.id && this.previous == cmp.previous && this.index == cmp.index && this.content == cmp.content;
    }
	@Override
	public int hashCode() {
		return Objects.hash(id, previous, index, content);
	}
    @Override
    public String toString() { 
    	return content + " (" + this.id + " " + Integer.toString(this.index) + ")";
    }
    
	
	//GETTERS
	public Relay getId() {
		return this.id;
	}
	
	public String getPrevious() {
		return this.previous;
	}

	public int getIndex() {
		return this.index;
	}
	
	public Object getContent() {
		return this.content;
	}
	
	public String getSignature() {
		return this.signature;
	}
	
	//ACTUAL IMPLEMENTATION
	public String getHash() {
		return Integer.toString(this.hashCode());	
	}
	
	public int getNextIndex() {
		return this.index+1;
	}
}
