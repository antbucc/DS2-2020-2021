package gossipingAppendOnlyLogs;

import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.eventGeneration.EventGenerationStrategy;
import gossipingAppendOnlyLogs.motion.MotionStrategy;
import gossipingAppendOnlyLogs.synchronization.SynchronizationStrategy;

public interface StrategyFactory {
	SynchronizationStrategy getCorrectStrategy(Person person);

	MotionStrategy getMotionStrategy(Person person);

	EventGenerationStrategy getEventGenerationStrategy(Person person);
}
