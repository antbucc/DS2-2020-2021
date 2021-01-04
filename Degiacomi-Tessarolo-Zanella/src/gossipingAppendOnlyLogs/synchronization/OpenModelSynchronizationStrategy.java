package gossipingAppendOnlyLogs.synchronization;

import gossipingAppendOnlyLogs.actors.Person;

public class OpenModelSynchronizationStrategy extends SynchronizationStrategy {

	public OpenModelSynchronizationStrategy(Person local) {
		super(local);
	}

	public void synchronize(Person remotePerson) {
		var remoteStore = remotePerson.getStore();
		createUnknownLogs(remoteStore.getIds());
		updateKnownIdsWithRemoteStore(remoteStore);
	}
}
