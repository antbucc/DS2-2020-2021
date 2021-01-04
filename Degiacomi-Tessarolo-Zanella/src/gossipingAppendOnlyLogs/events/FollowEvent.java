package gossipingAppendOnlyLogs.events;

import gossipingAppendOnlyLogs.models.PersonPublicKey;

public class FollowEvent extends Event {

    public final PersonPublicKey followedPerson;

    public FollowEvent(PersonPublicKey followedPerson) {
        this.followedPerson = followedPerson;
    }

	@Override
	protected String generateUniqueId() {
		lastSpecialEventId++;
		return "FollowEvent" + lastSpecialEventId;
	}

    @Override
    protected EventContentHash hashContent() {
        return new EventContentHash(followedPerson.hash());
    }

}
