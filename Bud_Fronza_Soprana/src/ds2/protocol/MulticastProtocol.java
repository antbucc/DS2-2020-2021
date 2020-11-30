package ds2.protocol;

// Standard libraries
import java.util.ArrayList;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.application.ApplicationData;
import ds2.protocol.messages.Soliton;
import ds2.protocol.messages.data.MulticastMsg;
import ds2.simulator.Oracle;

/**
 * Class that contains the functionalities for a multicast protocol.
 * It has as basis the broadcast protocol, which it uses to send and receive messages on the 
 * subscribed topics.
 * @author budge
 *
 */
public class MulticastProtocol {

	/**
	 * Broadcast protocol on which this multicast relies upon.
	 */
	private RealProtocol broadcastP;
	/**
	 * List of subscriptions.
	 */
	private ArrayList<Integer> myTopics;
	
	public MulticastProtocol(@NonNull RealProtocol broadcastP, ArrayList<Integer> topics) {
		this.broadcastP = broadcastP;
		this.myTopics = topics;
	}
	
	/**
	 * Get a list of subscriptions of this process.
	 * @return ArrayList<Integer> myTopics
	 */
	public ArrayList<Integer> getMyTopics() {
		return this.myTopics;
	}
	
	/**
	 * Subscribes to the given topics.
	 * @param topicId
	 */
	public void subscribeTo(int topicId) {
		this.myTopics.add(topicId);
	}
	
	/**
	 * Unsubscribe from the given topic.
	 * @param topicId
	 */
	public void unsubscribeFrom(int topicId) {
		int i = this.myTopics.indexOf(topicId);
		if (i!=-1)
			this.myTopics.remove(i);
	}
	
	/**
	 * Sends a multicast message on the given topic ID.
	 * @param topicId
	 * @param appData
	 */
	public void sendMulticastMsg(int topicId, ApplicationData appData) {
		broadcastP.sendSoliton(new MulticastMsg(topicId, appData));
	}
	
	/**
	 * Handles a multicast message passed to this protocol by the basis broadcast protocol.
	 * @param multiSoliton
	 */
	public void handleSoliton(Soliton multiSoliton) {
		MulticastMsg msg = (MulticastMsg) multiSoliton.getData();
		int topic = msg.getTopicId();
		if (myTopics.contains(topic)) {
			Oracle.mLog(this.broadcastP.getAddress(), "MulticastProtocol", "Message received on subscribed topic " + topic);
			
			broadcastP.application.handle(msg.getAppData());
		}
	}

}
