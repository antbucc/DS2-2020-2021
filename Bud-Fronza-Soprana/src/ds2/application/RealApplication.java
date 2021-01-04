package ds2.application;

// Standard libraries
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;

// Apache libraries
import org.apache.commons.lang3.tuple.Pair;

// Custom libraries
import ds2.application.UserActions.Action;
import ds2.application.events.BlockEvent;
import ds2.application.events.FollowEvent;
import ds2.application.events.GenerateEvent;
import ds2.application.events.UnBlockEvent;
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.application.events.UnFollowEvent;
import ds2.protocol.Protocol;
import ds2.protocol.SSBProtocol;
import ds2.protocol.SSBTransitiveInterest;
import ds2.simulator.Oracle;
import ds2.utility.logging.MachineEventHandler;
import ds2.visualization.DisplayManager;
import repast.simphony.random.RandomHelper;

// Repast libraries

/**
 * Real implementation of {@link Application} on top of {@link SSBProtocol}
 */
// TODO: Maybe change to TrackingApplication
public class RealApplication extends Application<MachineEventHandler<?, ?>, Protocol<?,?>> {
	
	public RealApplication(Protocol<?,?> down) {
		super(down);
	}

	/**
	 * Function to handle all ApplicationData. This call receives the data going upwards from the {@link RealProtocol}
	 * @param appData The application data to handle
	 */
	@ApplicationEventHandler(cls = ApplicationData.class)
	public void handleApplicationData(ApplicationData<?> appData) {
		if (appData.getData() instanceof Id) {
			log("Application", "Received " + appData);
		} else {
			err("Application", "Unexpected type of data received");
		}
	}


	/**
	 * Function to handle the ApplicationData generated locally. This call receives the data going upwards from the {@link RealProtocol}
	 * @param appData The application data to handle
	 */
	public void handleLocalApplicationData(ApplicationData<?> appData) {
		if (appData.getData() instanceof Id) {
			log("Application", "Created " + appData);
		} else {
			err("Application", "Unexpected type of data created");
		}
	}

	
	/**
	 * Function to handle a GenerateEvent
	 * @param ev the event to handle
	 */
	@ApplicationEventHandler(cls = GenerateEvent.class)
	public void handleGenerateEvent(GenerateEvent ev) {

		ApplicationData<?> ad = new ApplicationData<>(this.getAddress(), ev.getPort(), new Id());


		this.handleLocalApplicationData(ad);
		this.handleApplicationData(ad);
		
		// Send to protocol to take care of saving event and propagating it
		if (this.getDown() instanceof SSBProtocol) {
			((SSBProtocol)this.getDown()).createLocalEvent(ad);
		} else if (this.getDown() instanceof SSBTransitiveInterest){
			((SSBTransitiveInterest)this.getDown()).createLocalEvent(ad);
		} else {
			throw new RuntimeException("Unkown protocol type used");
		}
	}
	
	/**
	 * Function to handle a FollowEvent and start following a new identity
	 * @param ev the event to handle
	 */
	@ApplicationEventHandler(cls = FollowEvent.class)
	public void handleFollowEvent(FollowEvent ev) {
		
		HashSet<PublicKey> followedSet = ((SSBTransitiveInterest)this.getDown()).getFollowed();
		HashSet<PublicKey> blockedSet = ((SSBTransitiveInterest)this.getDown()).getBlocked();		
		
		// Check that the port is not already in followed or in blocked (first unblock)
		Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
		while ((this.getAddress().equals(addressAndPort.getKey()) &&
				this.getDown().getPort().equals(addressAndPort.getValue()) ) ||
				followedSet.contains(addressAndPort.getValue()) ||
				blockedSet.contains(addressAndPort.getValue())) {
			addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
		}		
		
		// When checks are done, follow
		UserActions follow = new UserActions(Action.FOLLOW, addressAndPort.getValue());
		ApplicationData<UserActions> ad = new ApplicationData<>(this.getAddress(), ev.getPort(), follow);
		
		// Show graphic follow
		DisplayManager.getInstance().showFollow(this.getMachine(), Oracle.getInstance().getMachine(addressAndPort.getLeft()));
		
		this.handleLocalApplicationData(ad);
		this.handleApplicationData(ad);
		((SSBTransitiveInterest)this.getDown()).createLocalEvent(ad);
	}
	
	/**
	 * Function to handle a UnFollowEvent (which will un-follow a previously followed identity)
	 * @param ev the event to handle
	 */
	@ApplicationEventHandler(cls = UnFollowEvent.class)
	public void handleUnFollowEvent(UnFollowEvent ev) {
		
		// Take an identity from the followed set
		ArrayList<PublicKey> followedSet = new ArrayList<>(((SSBTransitiveInterest)this.getDown()).getFollowed());
		
		if (!followedSet.isEmpty()) {
			PublicKey choice = followedSet.get(RandomHelper.nextIntFromTo(0, followedSet.size()-1));
			// After checks are done, unfollow
			UserActions unfollow = new UserActions(Action.UNFOLLOW, choice);
			ApplicationData<UserActions> ad = new ApplicationData<>(this.getAddress(), ev.getPort(), unfollow);
	
			// Show graphic unfollow
			Machine<?,?> unfollowed = Oracle.getInstance().fromKeyToMachine(choice);
			DisplayManager.getInstance().showUnFollow(this.getMachine(), unfollowed);
			
			this.handleLocalApplicationData(ad);
			this.handleApplicationData(ad);
			((SSBTransitiveInterest)this.getDown()).createLocalEvent(ad);
		}
	}
	
	/**
	 * Function to handle a BlockEvent (which will block another identity)
	 * @param ev the event to handle
	 */
	@ApplicationEventHandler(cls = BlockEvent.class)
	public void handleBlockEvent(BlockEvent ev) {
		
		HashSet<PublicKey> blockedSet = ((SSBTransitiveInterest)this.getDown()).getBlocked();
		Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
		
		// Check that the port is not in blocked set already 
		while ((this.getAddress().equals(addressAndPort.getKey()) &&
				this.getDown().getPort().equals(addressAndPort.getValue()) ) ||
				blockedSet.contains(addressAndPort.getValue())) {
			addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
		}
		
		// After checks are done, block
		UserActions block = new UserActions(Action.BLOCK, addressAndPort.getValue());
		ApplicationData<UserActions> ad = new ApplicationData<>(this.getAddress(), ev.getPort(), block);
		
		// Show graphic block
		DisplayManager.getInstance().showBlock(this.getMachine(), Oracle.getInstance().getMachine(addressAndPort.getLeft()));
		
		this.handleLocalApplicationData(ad);
		this.handleApplicationData(ad);
		((SSBTransitiveInterest)this.getDown()).createLocalEvent(ad);
	}
	
	/**
	 * Function to handle a UnBlockEvent (which will randomly unblock a previously blocked protocol)
	 * @param ev the event to handle
	 */
	@ApplicationEventHandler(cls = UnBlockEvent.class)
	public void handleUnBlockEvent(UnBlockEvent ev) {
		
		// Unblock an identity in the underlying blocked set
		ArrayList<PublicKey> blockedSet = new ArrayList<>(((SSBTransitiveInterest)this.getDown()).getBlocked());
		if (!blockedSet.isEmpty()) {
			PublicKey choice = blockedSet.get(RandomHelper.nextIntFromTo(0, blockedSet.size()-1));
			
			UserActions unblock = new UserActions(Action.UNBLOCK, choice);
			ApplicationData<UserActions> ad = new ApplicationData<>(this.getAddress(), ev.getPort(), unblock);
	
			// Show graphic unblock
			Machine<?,?> unblocked = Oracle.getInstance().fromKeyToMachine(choice);
			DisplayManager.getInstance().showUnBlock(this.getMachine(), unblocked);
			
			this.handleLocalApplicationData(ad);
			this.handleApplicationData(ad);
			((SSBTransitiveInterest)this.getDown()).createLocalEvent(ad);
		}
	}
}
