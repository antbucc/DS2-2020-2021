package gossipingAppendOnlyLogs.eventGeneration;

import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.events.StreamEvent;
import repast.simphony.engine.schedule.ScheduleParameters;

public class SimpleEventGenerationStrategy extends EventGenerationStrategy {

	public SimpleEventGenerationStrategy(Person person) {
		super(person);
		var params = ScheduleParameters.createUniformProbabilityRepeating(10, 0, 100, 50, 1);
		scheduler.schedule(params, this, "generateEvent");
	}

	@Override
	public void generateEvent() {
		var event = new StreamEvent("Some content");
		person.addEventToPersonalLog(event);
	}
}
