package gossipingAppendOnlyLogs.synchronization;

import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.models.PersonPublicKey;
import gossipingAppendOnlyLogs.models.Store;

import java.util.HashSet;
import java.util.Set;

public class TransitiveInterestSynchronizationStrategy extends SynchronizationStrategy {

    public TransitiveInterestSynchronizationStrategy(Person person) {
		super(person);
	}

	public void synchronize(Person remotePerson) {
    	var remoteStore = remotePerson.getStore();
		var toSync = computeIdsToSync(remoteStore, remotePerson.getPublicKey());
		createUnknownLogs(toSync);
		updateKnownIdsWithRemoteStore(remoteStore);
	}

	private Set<PersonPublicKey> computeIdsToSync(Store remoteStore, PersonPublicKey remoteId){
		var friends = localStore.getLogsFollowedBy(local.getPublicKey());
		var friendsOfRemote = remoteStore.getLogsFollowedBy(remoteId);
		var blocked = localStore.getLogsBlockedBy(local.getPublicKey());

		var toSync = new HashSet<PersonPublicKey>();
		toSync.add(remoteId); // synchronize with remote even if it's not my friend (we don't sync if I blocked him, see below)
		toSync.addAll(friends);
		toSync.addAll(friendsOfRemote); // transitive interest
		toSync.removeAll(blocked);
		return toSync;
	}
}
