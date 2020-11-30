package ds2.protocol.messages.data;

// Custom libraries
import ds2.application.ApplicationData;
import ds2.nodes.Address;
import ds2.protocol.messages.ProtocolMsg;

public class UnicastMsg extends ProtocolMsg {
	
	private Address dest; 
	
	public UnicastMsg(Address dest, ApplicationData appData) {
		super(appData);
		this.dest = dest;
	}

	public Address getDest() {
		return dest;
	}

	public void setDest(Address dest) {
		this.dest = dest;
	}


	@Override
	public String toString() {
		return " (UnicastMsg) <dest=" + dest + ", appData=" + this.appData +">";
	}

}
