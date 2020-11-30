package ds2.nodes.events;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.nodes.Address;
import ds2.nodes.LocalEvent;
import ds2.protocol.messages.Perturbation;

/**
 * This local event is used as a marker for the moment in time in which the node should, if possible, start transmitting a {@link Perturbation}
 */
public class BroadcastSend extends LocalEvent {
	public static int B_ID=0;
	
	public @Nullable Perturbation perturbation;
	public int bId;
	
	/**
	 * Constructor for BroadcastSend
	 * @param addr The address of the node which will broadcast
	 * @param perturbation The data to broadcast
	 */
	public BroadcastSend(@NonNull Address addr, @NonNull Perturbation perturbation) {
		super(addr);
		this.perturbation = perturbation;
		this.bId = B_ID;
		++B_ID;
	}
	
	public Perturbation getPerturbation() {
		return perturbation;
	}

	@Override
	public String csv() {
		return "bId=" + this.bId + ", source=" + this.getDestination().toString() + ", toBroadcast=" + perturbation.toString();
	}
}
