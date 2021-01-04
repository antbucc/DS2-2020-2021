package gossipingAppendOnlyLogs.actors;

import gossipingAppendOnlyLogs.RepastUtils;
import gossipingAppendOnlyLogs.StrategyFactory;
import gossipingAppendOnlyLogs.eventGeneration.EventGenerationStrategy;
import gossipingAppendOnlyLogs.events.Event;
import gossipingAppendOnlyLogs.models.Log;
import gossipingAppendOnlyLogs.models.PersonKeys;
import gossipingAppendOnlyLogs.models.PersonPublicKey;
import gossipingAppendOnlyLogs.models.Store;
import gossipingAppendOnlyLogs.motion.MotionStrategy;
import gossipingAppendOnlyLogs.synchronization.SynchronizationStrategy;
import repast.simphony.engine.schedule.ScheduledMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

public class Person {
    public final String id; // TODO: Do we need an ID? can we just use the public key, and make the toString show an hash?

    private final PersonKeys keys;

	private final Store store = new Store();

	private final SynchronizationStrategy synchronizationStrategy;
	private final MotionStrategy motionStrategy;
	private final EventGenerationStrategy eventGenerationStrategy;

    private final List<Event> createdEvents = new ArrayList<>(); // for log purposes
    public final List<Event> addedEvents = new ArrayList<>(); // for log purposes

	public Person(String id, PersonKeys keys, StrategyFactory strategyFactory) {
		this.id = id;
		this.keys = keys;
		createPersonalLog();
		this.synchronizationStrategy = strategyFactory.getCorrectStrategy(this);
		this.motionStrategy = strategyFactory.getMotionStrategy(this);
		this.eventGenerationStrategy = strategyFactory.getEventGenerationStrategy(this);
	}

    private void createPersonalLog() {
		store.add(new Log(keys.publicKey));
	}

    public PersonPublicKey getPublicKey() {
        return keys.publicKey;
    }

    @ScheduledMethod(start = 1, interval = 1)
    public void onTick() {
        motionStrategy.onTick();
        synchronizeWithConnectedPeople();
    }

	private void synchronizeWithConnectedPeople() {
		getConnectedPeople().forEach(synchronizationStrategy::synchronize);
	}

    private Set<Person> getConnectedPeople(){
		var lan = getConnectedLAN();
		if (lan.isEmpty()) {
			return emptySet();
		}
		return lan.get()
				.getConnectedPeople()
				.stream()
				.filter(person -> person != this)
				.collect(Collectors.toSet());
    }

	public Optional<LAN> getConnectedLAN() {
		return RepastUtils
				.getAllLANsInGrid()
				.stream()
				.map(LanWithDistance::new)
				.filter(LanWithDistance::canConnect)
				.min((a, b) -> ((int) (a.distance - b.distance)))
				.map(lanWithDistance -> lanWithDistance.lan);
	}

    private class LanWithDistance {
        final LAN lan;
        final double distance;

        public LanWithDistance(LAN lan) {
            this.lan = lan;
            this.distance = RepastUtils.getDistance(Person.this, lan);
        }

        public boolean canConnect(){
            return distance < lan.maximumDistance;
        }
    }

    public void addEventToPersonalLog(Event event){
    	store.get(keys.publicKey).appendEvent(keys, event);
    	createdEvents.add(event);
	}

    public Store getStore() {
    	return this.store;
    }

    //used by repast for logging purposes
    public String createdEvents() {
    	List<String> eventsStrings = createdEvents.stream()
    			.map(Event::toString)
    			.collect(Collectors.toList());

    	createdEvents.clear();

    	return String.join(",", eventsStrings);
    }

    //used by repast for logging purposes
    public String arrivedEvents() {
    	List<String> eventsStrings = addedEvents.stream()
    			.map(Event::toString)
    			.collect(Collectors.toList());

    	addedEvents.clear();

    	return String.join(",", eventsStrings);
    }

    public String getId() {
    	return id;
    }
}
