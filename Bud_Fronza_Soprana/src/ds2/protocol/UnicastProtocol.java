package ds2.protocol;

// Standard libraries
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.application.ApplicationData;
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.protocol.messages.Soliton;
import ds2.protocol.messages.data.UnicastMsg;
import ds2.simulator.Oracle;

/**
 * Class for point to point communcation
 * @author budge
 *
 */
public class UnicastProtocol{
	/**
	 * Broadcast Protocol on which this unicast relies upon.
	 */
	private RealProtocol broadcastP;
	/**
	 * List of known peers.
	 */
	ArrayList<Address> knownPeers;
	public UnicastProtocol(@NonNull RealProtocol broadcastP) {
		
		this.broadcastP = broadcastP;
		knownPeers = new ArrayList<>();
		
	}
	
	/**
	 * Update list of peers.
	 * @param addr
	 */
	public void addPeer(Address addr) {
		this.knownPeers.add(addr);
	}
	
	/**
	 * Send unicast message to a destination.
	 * @param dest
	 * @param appData
	 */
	public void sendUnicastMsg(Address dest, ApplicationData appData) {
		broadcastP.sendSoliton(new UnicastMsg(dest, appData));
	}
	
	/**
	 * Handle solitons containing unicast messages.
	 * @param p2pSoliton
	 */
	public void handleSoliton(Soliton p2pSoliton) {
		
		Address source = p2pSoliton.getSource();
		UnicastMsg msg = (UnicastMsg) p2pSoliton.getData();
		Address dest = msg.getDest();
		// For me
		if (dest.equals(broadcastP.runningOn.getAddress())) {
			Oracle.mLog(this.broadcastP.getAddress(), "UnicastProtocol", "Unicast message received from " + source + ". Is for me");
			
			broadcastP.application.handle(msg.getAppData());
		
		} else {
			Oracle.mLog(this.broadcastP.getAddress(), "UnicastProtocol", "Unicast message received from " + source + ". Is not for me");
		}
	}
	
	/**
	 *  Can be called outside, to make sure we send addresses to peers which are alive.
	 */
	public ArrayList<Address> getAlivePeers(){
		
		ArrayList<Address> alive;

		ArrayList<Machine> allMachines = Oracle.getInstance().getAllMachines();
		Set<Address> machines = (HashSet<Address>) allMachines
				.stream().map((m) -> m.getAddress()).collect(Collectors.toSet());
       
        HashSet<Address> alivePeers = new HashSet<>(machines);
        alivePeers.retainAll(knownPeers);

        alive = new ArrayList<Address>(alivePeers);        
        
		return alive;
	}
	
	
}
