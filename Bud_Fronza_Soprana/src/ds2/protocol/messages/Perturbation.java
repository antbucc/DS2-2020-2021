package ds2.protocol.messages;

// Custom libraries
import ds2.nodes.Address;

public abstract class Perturbation {
	
	protected Address source; 

	public Perturbation (Address source){
		this.source = source;
	}
	
	
	public Address getSource() {
		return source;
	}


	public void setSource(Address source) {
		this.source = source;
	}
	
	
	public String toString() {
		return "src:" + source.toString();
	}
	
	
}
