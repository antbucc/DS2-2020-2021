package distributedgroup_project1.messages;

public abstract class Message {
	private final String val;
	private final int size;
	
	public Message(String val, int size) {
		super();
		this.val = val;
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public String getVal() {
		return val;
	}
}
