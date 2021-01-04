package gossipingAppendOnlyLogs.events;

import gossipingAppendOnlyLogs.models.PersonPublicKey;

public class BlockEvent extends Event {
    public final PersonPublicKey blockedPerson;

    public BlockEvent(PersonPublicKey blockedPerson) {
        this.blockedPerson = blockedPerson;
    }

	@Override
	protected String generateUniqueId() {
		lastSpecialEventId++;
		return "BlockEvent" + lastSpecialEventId;
	}

    @Override
    protected EventContentHash hashContent() {
        return new EventContentHash(blockedPerson.hash());
    }

}
