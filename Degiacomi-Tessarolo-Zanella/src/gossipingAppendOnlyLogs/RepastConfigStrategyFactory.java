package gossipingAppendOnlyLogs;

import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.eventGeneration.EventGenerationStrategy;
import gossipingAppendOnlyLogs.eventGeneration.EventGenerationStrategyWithInterests;
import gossipingAppendOnlyLogs.eventGeneration.SimpleEventGenerationStrategy;
import gossipingAppendOnlyLogs.motion.HabitMotionStrategy;
import gossipingAppendOnlyLogs.motion.MotionStrategy;
import gossipingAppendOnlyLogs.motion.RandomMotionStrategy;
import gossipingAppendOnlyLogs.synchronization.OpenModelSynchronizationStrategy;
import gossipingAppendOnlyLogs.synchronization.SynchronizationStrategy;
import gossipingAppendOnlyLogs.synchronization.TransitiveInterestSynchronizationStrategy;
import repast.simphony.engine.environment.RunEnvironment;

public class RepastConfigStrategyFactory implements StrategyFactory {

	private static final String OPEN_MODEL = "OpenModel";
	private static final String TRANSITIVE_INTEREST = "TransitiveInterest";

	@Override
	public SynchronizationStrategy getCorrectStrategy(Person person) {
		String strategy = getSynchronizationProtocol();
		if (strategy.equals(OPEN_MODEL)) {
			return new OpenModelSynchronizationStrategy(person);
		} else {
			return new TransitiveInterestSynchronizationStrategy(person);
		}
	}

	@Override
	public MotionStrategy getMotionStrategy(Person person) {
		var params = RunEnvironment.getInstance().getParameters();
		var motionStrategy = params.getString("motionStrategy");
		if(motionStrategy.equals("HabitMotion"))
			return new HabitMotionStrategy(person);
		else
			return new RandomMotionStrategy(person);
	}

	@Override
	public EventGenerationStrategy getEventGenerationStrategy(Person person) {
		String strategy = getSynchronizationProtocol();
		if (strategy.equals(OPEN_MODEL)) {
			return new SimpleEventGenerationStrategy(person);
		} else {
			return new EventGenerationStrategyWithInterests(person);
		}
	}

	private static String getSynchronizationProtocol() {
		var params = RunEnvironment.getInstance().getParameters();
		return params.getString("synchronizationProtocol");
	}
}
