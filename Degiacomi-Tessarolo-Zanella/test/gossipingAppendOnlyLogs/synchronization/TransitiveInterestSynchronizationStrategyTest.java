package gossipingAppendOnlyLogs.synchronization;

import gossipingAppendOnlyLogs.StrategyFactory;
import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.eventGeneration.EventGenerationStrategy;
import gossipingAppendOnlyLogs.events.BlockEvent;
import gossipingAppendOnlyLogs.events.FollowEvent;
import gossipingAppendOnlyLogs.events.StreamEvent;
import gossipingAppendOnlyLogs.motion.MotionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gossipingAppendOnlyLogs.CryptographyUtils.generateKeys;
import static org.junit.jupiter.api.Assertions.*;

class TransitiveInterestSynchronizationStrategyTest {
	private Person alice;
	private Person bob;
	private Person charlie;
	private Person dave;
	private Person eve;

	@BeforeEach
	public void setup() {
		alice = createPerson("alice");
		bob = createPerson("bob");
		charlie = createPerson("charlie");
		dave = createPerson("dave");
		eve = createPerson("eve");
	}

	@Test
	public void aliceDiscoverBobEmptyLog() {
		// given
		var aliceSynchronizationStrategy = new TransitiveInterestSynchronizationStrategy(alice);

		// when
		aliceSynchronizationStrategy.synchronize(bob);

		// then
		assertTrue(alice.getStore().getIds().contains(bob.getPublicKey()));
	}

	@Test
	public void aliceDiscoverBobLogWithOneEvent() {
		// given
		bob.addEventToPersonalLog(new StreamEvent("My first post"));
		var aliceSynchronizationStrategy = new TransitiveInterestSynchronizationStrategy(alice);

		// when
		aliceSynchronizationStrategy.synchronize(bob);

		// then
		assertEquals(1, alice.getStore().get(bob.getPublicKey()).getHeight());
	}

	@Test
	public void BobFollowsCharlie_AliceDiscoversBobAndTransitivelyCharlie() {
		// given
		bob.addEventToPersonalLog(new FollowEvent(charlie.getPublicKey()));
		var aliceSynchronizationStrategy = new TransitiveInterestSynchronizationStrategy(alice);

		// when
		aliceSynchronizationStrategy.synchronize(bob);

		// then
		assertTrue(alice.getStore().getIds().contains(bob.getPublicKey()));
		assertTrue(alice.getStore().getIds().contains(charlie.getPublicKey()));
	}

	@Test
	public void BobFollowsCharlie_AliceDiscoversBobAndTransitivelyCharlie_AliceDoNotDiscoverDave() {
		// given
		bob.addEventToPersonalLog(new FollowEvent(charlie.getPublicKey()));
		charlie.addEventToPersonalLog(new FollowEvent(dave.getPublicKey()));
		var aliceSynchronizationStrategy = new TransitiveInterestSynchronizationStrategy(alice);

		// when
		aliceSynchronizationStrategy.synchronize(bob);

		// then
		assertTrue(alice.getStore().getIds().contains(bob.getPublicKey()));
		assertTrue(alice.getStore().getIds().contains(charlie.getPublicKey()));
		assertFalse(alice.getStore().getIds().contains(dave.getPublicKey()));
	}

	@Test
	public void BobFollowsCharlieAndEve_AliceBlockedEve_AliceDiscoversBobAndTransitivelyCharlie() {
		// given
		bob.addEventToPersonalLog(new FollowEvent(charlie.getPublicKey()));
		bob.addEventToPersonalLog(new FollowEvent(eve.getPublicKey()));
		alice.addEventToPersonalLog(new BlockEvent(eve.getPublicKey()));
		var aliceSynchronizationStrategy = new TransitiveInterestSynchronizationStrategy(alice);

		// when
		aliceSynchronizationStrategy.synchronize(bob);

		// then
		assertTrue(alice.getStore().getIds().contains(bob.getPublicKey()));
		assertTrue(alice.getStore().getIds().contains(charlie.getPublicKey()));
		assertFalse(alice.getStore().getIds().contains(eve.getPublicKey()));
	}

	private Person createPerson(String id) {
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