package ds2.protocol.messages.data;

// Custom libraries
import ds2.application.ApplicationData;
import ds2.protocol.messages.ProtocolMsg;

public class MulticastMsg extends ProtocolMsg{

	private int topicId;
	
	public MulticastMsg(int topicId, ApplicationData appData) {
		super(appData);
		this.topicId = topicId;
	}

	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	

	@Override
	public String toString() {
		return " (MulticastMsg) <topic=" + topicId + ", appData=" + this.appData + ">";
	}

}
