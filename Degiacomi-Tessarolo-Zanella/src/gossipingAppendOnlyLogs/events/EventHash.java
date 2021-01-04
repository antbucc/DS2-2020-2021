package gossipingAppendOnlyLogs.events;

import gossipingAppendOnlyLogs.HashUtils;

public class EventHash {

	public final String hash;
	
    public static EventHash of(Event event) {
        if (!event.isSigned()) {
            throw new IllegalStateException("Event must be signed before computing the hash");
        }

    	var description = event.getCreatorId().hash() + event.getPreviousEventHash() + event.hashContent() + event.getSignature();
        var hash = HashUtils.hash(description);
        return new EventHash(hash);
    }

	public EventHash(String hash) {
		this.hash = hash;
	}
	
	@Override
	public String toString() {
		return hash;
	}
}
