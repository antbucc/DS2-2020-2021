package gossipingAppendOnlyLogs.eventGeneration;

import gossipingAppendOnlyLogs.RepastUtils;
import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.events.*;
import gossipingAppendOnlyLogs.models.PersonPublicKey;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.random.RandomHelper;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class EventGenerationStrategyWithInterests extends EventGenerationStrategy {

	private static final String streamEventContent = "Some content";

	public EventGenerationStrategyWithInterests(Person person) {
		super(person);
		var params = ScheduleParameters.createUniformProbabilityRepeating(10, 0, 100, 50, 1);
		var paramsSpecialEvents = ScheduleParameters.createUniformProbabilityRepeating(10, 0, 100, 50, 1);
		scheduler.schedule(params, this, "generateEvent");
		scheduler.schedule(paramsSpecialEvents, this, "generateInterestEvent");
	}

	/*every now and then, create a stream event (this makes sure that a person actively
	 *posts new events and doesn't only follow/unfollow other members of the system
	 */
	@Override
	public void generateEvent() {
		var event = new StreamEvent(streamEventContent);
		person.addEventToPersonalLog(event);
	}

	public void generateInterestEvent() {
		Event event;
		//used to exit the loop in the case the person is already following everybody else
		
		do {
			int choice = RandomHelper.nextIntFromTo(0, 9);
			//a followEvent is the most probable event type
			if (choice <= 4)
				event = createFollowEvent();
			else if (choice <= 7) 
				event = createUnfollowEvent();
			else if (choice <= 8)
				event = createBlockEvent();
			else
				event = createUnblockEvent();
			
		} while (event == null);
		
		person.addEventToPersonalLog(event);
	}

	protected FollowEvent createFollowEvent() {
		var people = getAllPeople();

		var alreadyFollowing = person
				.getStore()
				.getLogsFollowedBy(person.getPublicKey());

		var blocked = person
				.getStore()
				.getLogsBlockedBy(person.getPublicKey());

		//I don't want to start following persons Im already following or have blocked
		people.removeAll(alreadyFollowing);
		people.removeAll(blocked);

		if (people.isEmpty()) {
			return null;
		}
		return new FollowEvent(pickRandomPerson(people));
	}

	protected UnfollowEvent createUnfollowEvent() {
		var following = person
				.getStore()
				.getLogsFollowedBy(person.getPublicKey());

		// TODO: can I follow a person I've blocked?
		Set<PersonPublicKey> blocked = person
				.getStore()
				.getLogsBlockedBy(person.getPublicKey());

		following.removeAll(blocked);

		if (following.isEmpty()) {
			return null;
		}

		return new UnfollowEvent(pickRandomPerson(following));
	}

	protected BlockEvent createBlockEvent() {
		var blocked = person
				.getStore()
				.getLogsBlockedBy(person.getPublicKey());

		var blockablePeople = getAllPeople()
				.stream()
				.filter(person -> !blocked.contains(person))
				.collect(Collectors.toSet());

		if (blockablePeople.isEmpty()) {
			return null;
		}

		return new BlockEvent(pickRandomPerson(blockablePeople));
	}

	private Set<PersonPublicKey> getAllPeople() {
		return RepastUtils
				.getAllPeopleInGrid()
				.stream()
				.map(Person::getPublicKey)
				.collect(Collectors.toSet());
	}

	protected UnblockEvent createUnblockEvent() {
		Set<PersonPublicKey> blocked = person
				.getStore()
				.getLogsBlockedBy(person.getPublicKey());

		if (blocked.isEmpty()) {
			return null;
		}

		return new UnblockEvent(pickRandomPerson(blocked));
	}

	private static PersonPublicKey pickRandomPerson(Set<PersonPublicKey> persons) {
		if (persons.isEmpty()) {
			throw new IllegalArgumentException("persons set is empty");
		}
		int item = RandomHelper.nextIntFromTo(0, persons.size() - 1);
		return new ArrayList<>(persons).get(item);
	}
}

