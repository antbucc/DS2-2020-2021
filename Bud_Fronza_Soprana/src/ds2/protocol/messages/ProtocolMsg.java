package ds2.protocol.messages;

// Custom libraries
import ds2.application.ApplicationData;

public abstract class ProtocolMsg {
	
	protected ApplicationData appData;
	
	public ProtocolMsg(ApplicationData appData) {
		this.appData = appData;
	}
	
	public ApplicationData getAppData() {
		return appData;
	}

	public void setAppData(ApplicationData appData) {
		this.appData = appData;
	}

	// TODO
	public void encrypt(Object key, ApplicationData appData) {
		return;
	}
	
	public String toString() {
		return "PMsg data: " + this.appData;
	}
	
}
