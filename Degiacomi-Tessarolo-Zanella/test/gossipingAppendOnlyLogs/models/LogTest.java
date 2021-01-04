package gossipingAppendOnlyLogs.models;

import gossipingAppendOnlyLogs.CryptographyUtils;
import gossipingAppendOnlyLogs.actors.Person;
import gossipingAppendOnlyLogs.events.StreamEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gossipingAppendOnlyLogs.CryptographyUtils.generateKeys;
import static org.junit.jupiter.api.Assertions.*;

class LogTest {

	private final PersonKeys keys = generateKeys();
	private Log log;

	@BeforeEach
	public void setup(){
		log = new Log(keys.publicKey);
	}

	@Test
	public void getLastReturnsMinusOneWhenEmpty() {
		// when
		var last = log.getLast();

		// then
		assertEquals(-1, last);
	}

	@Test
	public void getLastReturnsInclusiveIndex() {
		// given
		log.appendEvent(keys, new StreamEvent("something"));

		// when
		var last = log.getLast();

		// then
		assertEquals(0, last);
	}

	@Test
	public void getEventsIsInclusive() {
		// given
		log.appendEvent(keys, new StreamEvent("something"));

		// then
		assertEquals(0, log.getEvents(-1, -1).size());
		assertEquals(1, log.getEvents(-1, 0).size());
		assertEquals(1, log.getEvents(0, 0).size());
	}
}