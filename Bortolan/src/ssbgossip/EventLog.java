package ssbgossip;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import analysis.EventAnalysis;
import exceptions.ChainErrorException;
import exceptions.WrongLogException;

public class EventLog {

	private final PublicKey publicKey;
	public final String id;
	public int last = 0;
	private String lastHash = null;
	private final List<Event> log;
	private final Participant owner;
	private final EventAnalysis ea;

	public EventLog(String id, Participant owner, EventAnalysis ea) throws Exception {
		this.id = id;
		this.owner = owner;
		this.ea = ea;

		// retrieve PublicKeay from id
		publicKey = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(id)));

		log = new ArrayList<>();
	}

	public void append(Map<String, String> content, PrivateKey privateKey) throws Exception {
		last++;

		String msg = id + lastHash + last + content;

		/* The following lines are commented to increase the simulator performance */
//		byte[] sig = ECCipher.sign(privateKey, msg);
//		String signature = Base64.getEncoder().encodeToString(sig);
		String signature = "null";

		/* The following lines are commented to increase the simulator performance */
//		if (!ECCipher.verify(publicKey, sig, msg)) {
//			throw new WrongLogException();
//		}

		Event e = new Event(id, lastHash, last, content, signature);
		log.add(e);
		lastHash = computeHash(e);
		ea.created(e, owner);
	}

	public void update(List<Event> events) throws Exception {
		Iterator<Event> it = events.iterator();
		while (it.hasNext()) {
			Event e = it.next();
			// check if event belongs to this log
			if (e.getId() != id) {
				throw new WrongLogException();
			}

			// check if event is authentic
			String msg = e.getId() + e.getPrevious() + e.getIndex() + e.getContent();

			/* The following lines are commented to increase the simulator performance */
//			byte[] sig = Base64.getDecoder().decode(e.getSignature());
//			if (!ECCipher.verify(publicKey, sig, msg)) {
//				throw new WrongLogException();
//			}
			
			// check if event respect chain rules
			if ((e.getIndex() != last + 1) || (e.getPrevious() == null && e.getPrevious() != lastHash)
					|| (e.getPrevious() != null && !e.getPrevious().equals(lastHash))) {
				throw new ChainErrorException();
			}

			this.last++;
			this.log.add(e);
			lastHash = computeHash(e);
			ea.received(e, owner);
		}
	}

	public List<Event> get(int start, int end) {
		List<Event> events = new ArrayList<>();
		log.forEach((e) -> {
			if (e.getIndex() > start && e.getIndex() <= end) {
				events.add(e);
			}
		});
		return events;
	}

	public int size() {
		return this.log.size();
	}

	public List<String> getFollowed() {
		List<String> followed = new ArrayList<>();
		Iterator<Event> it = log.iterator();
		while (it.hasNext()) {
			Event e = it.next();
			String addId = e.getContent().get("follow");
			if (addId != null) {
				followed.add(addId);
			}
			String delId = e.getContent().get("unfollow");
			if (delId != null) {
				followed.remove(delId);
			}
		}
		return followed;
	}

	public List<String> getBlocked() {
		List<String> blocked = new ArrayList<>();
		Iterator<Event> it = log.iterator();
		while (it.hasNext()) {
			Event e = it.next();
			String addId = e.getContent().get("block");
			if (addId != null) {
				blocked.add(addId);
			}
			String delId = e.getContent().get("unblock");
			if (delId != null) {
				blocked.remove(delId);
			}
		}
		return blocked;
	}

	public String toString() {
		String s = id + ": [";
		Iterator<Event> it = log.iterator();
		while (it.hasNext()) {
			Event e = it.next();
			s += e.getIndex() + ", ";
		}
		s += "]";
		return s;
	}

	public static String computeHash(Event event) {
		String hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] hashbytes = digest.digest(event.toJSON().getBytes(StandardCharsets.UTF_8));
			hash = Base64.getEncoder().encodeToString(hashbytes);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash;
	}

}
