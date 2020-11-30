package ds2.protocol.messages.data;

// Custom libraries
import ds2.application.ApplicationData;
import ds2.protocol.messages.ProtocolMsg;

public class BroadcastMsg extends ProtocolMsg{

	public BroadcastMsg(ApplicationData appData) {
		super(appData);
	}
	
	@Override
	public String toString() {
		return " (BroadcastMsg) appData=" + this.appData;
	}
}
