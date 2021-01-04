package gossipingAppendOnlyLogs;

import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.eventGeneration.EventGenerationStrategy;
import gossipingAppendOnlyLogs.motion.MotionStrategy;
import gossipingAppendOnlyLogs.synchronization.SynchronizationStrategy;

import static gossipingAppendOnlyLogs.CryptographyUtils.generateKeys;

public class Utils {
	public static Person createPerson(String id) {
		return new Person(id, generateKeys(), new StrategyFactory() {
			@Override
			public SynchronizationStrategy getCorrectStrategy(Person person) {
				return null;
			}

			@Override
			public MotionStrategy getMotionStrategy(Person person) {
				return null;
			}

			@Override
			public EventGenerationStrategy getEventGenerationStrategy(Person person) {
				return null;
			}
		});
	}
}
