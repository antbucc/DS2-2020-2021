package ds2.application;

// Standard libraries
import java.util.ArrayList;

// Custom libraries
import ds2.application.events.GenerateBroadcastEvent;
import ds2.application.events.GenerateMulticastEvent;
import ds2.application.events.GenerateUnicastEvent;
import ds2.nodes.Address;
import ds2.protocol.RealProtocol;
import ds2.simulator.Oracle;

// Repast libraries
import repast.simphony.random.RandomHelper;

/**
 * Real implementation of {@link Application} on top of {@link RealProtocol}
 */
public class RealApplication extends Application<RealProtocol> {

	@Override
	public void handle(ApplicationData appData) {
		if (appData instanceof Id) {
			Oracle.mLog(this.getAddress(), "Application", "Received " + appData);
		} else {
			Oracle.mErr(this.getAddress(), "Application", "Unexpected type of data received");
		}
	}

	@Override
	public void handleApplicationEvent(ApplicationEvent applicationEvent) {
		if (applicationEvent instanceof GenerateBroadcastEvent) {
			Id fakeData = new Id();
			this.handle(fakeData);
			
			this.protocol.sendBroadcastMsg(fakeData);
		} else if (applicationEvent instanceof GenerateMulticastEvent) {
			Id fakeData = new Id();
			this.handle(fakeData);
			
			ArrayList<Integer> myTopics = this.protocol.getMulticast().getMyTopics();
			int i = RandomHelper.nextIntFromTo(0, myTopics.size()-1);
			int random_topic_id = myTopics.get(i);
			
			this.protocol.getMulticast().sendMulticastMsg(random_topic_id, fakeData);
		} else if (applicationEvent instanceof GenerateUnicastEvent) {
			ArrayList<Address> alivePeers = this.protocol.getUnicast().getAlivePeers();
			Address aliveDest = null;
			
			Id fakeData = new Id();
			this.handle(fakeData);
			
			if (!alivePeers.isEmpty()) {
				aliveDest = alivePeers.get(RandomHelper.nextIntFromTo(0, alivePeers.size()-1));
			}
			if (aliveDest != null) {
				this.protocol.getUnicast().sendUnicastMsg(aliveDest, fakeData);
			} else {
				Oracle.mErr(this.getAddress(), "Application", "No peer alive. Can't send unicast");
			}
		}
	}	
}
