package gossipingAppendOnlyLogs.models;

import gossipingAppendOnlyLogs.events.StreamEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static gossipingAppendOnlyLogs.CryptographyUtils.generateKeys;
import static org.junit.jupiter.api.Assertions.*;

class StoreTest {

	private Store store;

	private Log logOfAlice;

	private final PersonKeys keysOfAlice = generateKeys();

	@BeforeEach
	void setUp() {
		store = new Store();
		logOfAlice = new Log(keysOfAlice.publicKey);
		store.add(logOfAlice);
	}

	@Test
	public void getFrontier(){
		// given
		logOfAlice.appendEvent(keysOfAlice, new StreamEvent("something"));

		// when
		var frontier = store.getFrontier(Collections.singleton(keysOfAlice.publicKey));

		// then
		assertEquals(1, frontier.getSize());
		assertEquals(0, frontier.getItem(0).last);
	}
}