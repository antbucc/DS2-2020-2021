package gossipingAppendOnlyLogs.events;

import gossipingAppendOnlyLogs.models.PersonKeys;
import gossipingAppendOnlyLogs.models.PersonPublicKey;


public abstract class Event {

	protected static int lastEventId = -1; // used for logging purposes
	protected static int lastSpecialEventId = -1; // used for logging purposes, special events are follows/unfollows/...

    protected PersonPublicKey creatorId;
    protected EventHash previousEventHash;
    protected EventSignature signature;
    
    protected String uniqueId; // used for logging purposes only

	protected EventHash hashAfterSignature;

	public final void sign(PersonKeys keys, EventHash previousEventHash) {
		if (hashAfterSignature != null) {
			throw new IllegalStateException("event has already been signed");
		}
		this.uniqueId = generateUniqueId();
		this.creatorId = keys.publicKey;
		this.previousEventHash = previousEventHash;
		this.signature = EventSignature.of(this, keys);
		hashAfterSignature = EventHash.of(this);
	}

	protected abstract String generateUniqueId();

    protected abstract EventContentHash hashContent();

    public EventHash hash() {
		if (hashAfterSignature == null) {
			throw new IllegalStateException("event has not be signed yet");
		}
		return hashAfterSignature;
    }

    public boolean isSigned() {
        return signature != null;
    }

    public EventSignature getSignature() {
        return signature;
    }

    public PersonPublicKey getCreatorId() {
        return creatorId;
    }

    public EventHash getPreviousEventHash() {
        return previousEventHash;
    }
    
    @Override
    public String toString() {
    	return uniqueId;
    }
}
