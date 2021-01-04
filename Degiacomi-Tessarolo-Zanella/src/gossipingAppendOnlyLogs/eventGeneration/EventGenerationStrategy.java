package gossipingAppendOnlyLogs.eventGeneration;

import gossipingAppendOnlyLogs.actors.Person;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;

public abstract class EventGenerationStrategy {

	protected final ISchedule scheduler = RunEnvironment.getInstance().getCurrentSchedule();

	protected final Person person;

	protected EventGenerationStrategy(Person person) {
		this.person = person;
	}

	protected abstract void generateEvent();
}
