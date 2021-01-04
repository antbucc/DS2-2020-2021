package gossipingAppendOnlyLogs.events;

import gossipingAppendOnlyLogs.models.PersonPublicKey;

public class UnfollowEvent extends Event {

    public final PersonPublicKey unfollowedPerson;

    public UnfollowEvent(PersonPublicKey unfollowedPerson) {
        this.unfollowedPerson = unfollowedPerson;
    }

	@Override
	protected String generateUniqueId() {
		lastSpecialEventId++;
		return "UnfollowEvent" + lastSpecialEventId;
	}

    @Override
    protected EventContentHash hashContent() {
        return new EventContentHash(unfollowedPerson.hash());
    }
}
