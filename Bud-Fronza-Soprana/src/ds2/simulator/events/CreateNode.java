package ds2.simulator.events;

// Standard libraries
import java.util.ArrayList;

// Custom libraries
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.protocol.Protocol;
import ds2.protocol.ssb.Identity;
import ds2.simulator.SimulationEvent;

/**
 * This events triggers the creation of a node
 */
public class CreateNode extends SimulationEvent {
	ArrayList<Identity> identities = null;
	ArrayList<Protocol> protocols = null;
	Machine machine;
	/**
	 * The constructor for the event
	 * @param addr The address of the new node
	 * @param identities The list of identities to assign to the node when is created
	 */
//	public CreateNode(Address addr, ArrayList<Identity> identities) {
//		super(addr);
//		this.identities = identities;
//	}
	
	/**
	 * The constructor for the event
	 * @param addr The address of the new node
	 * @param identities The list of identities to assign to the node when is created
	 */
	public CreateNode(Address addr, ArrayList<Protocol> protocols, Machine machine) {
		super(addr);
		this.protocols = protocols;
		this.machine = machine;
	}
	
	public ArrayList<Identity> getIdentities() {
		return identities;
	}
	
	public ArrayList<Protocol> getProtocols(){
		return protocols;
	}
	
	public Machine getMachine() {
		return machine;
	}
	
	public String toString() {
		return super.toString();
	}
}
