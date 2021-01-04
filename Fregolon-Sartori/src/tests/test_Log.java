package tests;

import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.List;

import junit.framework.TestCase;
import ssbGossip.Event;
import ssbGossip.Log;

public class test_Log extends TestCase {
	Log log;

	protected void setUp() throws Exception {
		super.setUp();
		PublicKey id = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();
		log = new Log(id);
		//TODO: Implement signatures in test
//		log.add(new Event(id, null, 7, null, null));
//		log.add(new Event(id, log.getLatest(), 10, null, null));
//		log.add(new Event(id, log.getLatest(), 4, null, null));
//		log.add(new Event(id, log.getLatest(), 6, null, null));
//		log.add(new Event(id, log.getLatest(), 2, null, null));
//		log.add(new Event(id, log.getLatest(), 5, null, null));
//		log.add(new Event(id, log.getLatest(), 3, null, null));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLog() {
	}
	
	public void testAdd() {
		List<Event> events = log.getSince(0);
		for (int i = 1; i < events.size(); i++)
			assertTrue(events.get(i - 1).getIndex() <= events.get(i).getIndex());
	}

	public void testGetLatest() {
		Event e = log.getLatest();
		assertEquals(10, e.getIndex());
	}

	public void testGetSince() {
		List<Event> events = log.getSince(5);
		assertEquals(3, events.size());
		assertEquals(6, events.get(0).getIndex());
		assertEquals(7, events.get(1).getIndex());
		assertEquals(10, events.get(2).getIndex());
	}

}
