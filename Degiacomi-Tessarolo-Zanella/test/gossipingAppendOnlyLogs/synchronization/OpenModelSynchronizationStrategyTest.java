package gossipingAppendOnlyLogs.synchronization;

import gossipingAppendOnlyLogs.Utils;
import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.events.StreamEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenModelSynchronizationStrategyTest {

	private Person alice;
	private Person bob;

	@BeforeEach
	public void setup(){
		alice = Utils.createPerson("alice");
		bob = Utils.createPerson("bob");
	}

	@Test
	public void aliceDiscoverBobEmptyLog() {
		// given
		var aliceSynchronizationStrategy = new OpenModelSynchronizationStrategy(alice);

		// when
		aliceSynchronizationStrategy.synchronize(bob);

		// then
		assertTrue(alice.getStore().getIds().contains(bob.getPublicKey()));
	}

	@Test
	public void aliceDiscoverBobLogWithOneEvent() {
		// given
		bob.addEventToPersonalLog(new StreamEvent("My first post"));
		var aliceSynchronizationStrategy = new OpenModelSynchronizationStrategy(alice);

		// when
		aliceSynchronizationStrategy.synchronize(bob);

		// then
		assertEquals(1, alice.getStore().get(bob.getPublicKey()).getHeight());
	}

}
