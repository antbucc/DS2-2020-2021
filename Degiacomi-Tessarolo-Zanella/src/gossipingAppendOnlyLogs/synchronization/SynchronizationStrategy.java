package gossipingAppendOnlyLogs.synchronization;

import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.models.Log;
import gossipingAppendOnlyLogs.models.PersonPublicKey;
import gossipingAppendOnlyLogs.models.Store;

import java.util.Set;

public abstract class SynchronizationStrategy {

	protected final Person local;
	protected final Store localStore;

	public SynchronizationStrategy(Person local) {
		this.local = local;
		this.localStore = local.getStore();
	}

	public abstract void synchronize(Person remotePerson);

	protected void createUnknownLogs(Set<PersonPublicKey> toSync) {
		var knownIds = localStore.getIds();
		toSync.stream()
				.filter(id -> !knownIds.contains(id))
				.map(Log::new)
				.forEach(localStore::add);
	}

	protected void updateKnownIdsWithRemoteStore(Store remoteStore) {
		// we should now update the stores
		var frontier = localStore.getFrontier(localStore.getIds());
		var news = remoteStore.getEventsSince(frontier);
		localStore.update(news);

		// save new events to a list for logging purposes
		local.addedEvents.addAll(news);
	}
}
