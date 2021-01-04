package gossipingAppendOnlyLogs.events;

import gossipingAppendOnlyLogs.CryptographyUtils;
import gossipingAppendOnlyLogs.models.PersonKeys;

public class EventSignature {

	public final String signature;

	public static EventSignature of(Event event, PersonKeys keys) {
    	var description = event.getCreatorId().hash() + event.getPreviousEventHash() + event.hashContent();
		var signature = CryptographyUtils.encrypt(keys.privateKey.key, description);
		return new EventSignature(signature);
    }

	public EventSignature(String signature) {
		this.signature = signature;
	}
	
	@Override
	public String toString() {
		return signature;
	}
}
