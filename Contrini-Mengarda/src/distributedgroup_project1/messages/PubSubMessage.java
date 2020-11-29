package distributedgroup_project1.messages;

public class PubSubMessage extends Message {
	private final int topic;
	
	public PubSubMessage(int topic, String val, int size) {
		super(val, size);
		this.topic = topic;
	}

	public int getTopic() {
		return topic;
	}	
}
