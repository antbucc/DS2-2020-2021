package ssbgossip;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import analysis.EventAnalysis;
import exceptions.ChainErrorException;
import exceptions.WrongLogException;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import ssbgossip.Message.MsgType;

public class Participant {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Context<Object> context;
	private Network<Object> connNet;
	private Network<Object> socialNet;
	private Network<Object> blockedNet;

	private KeyPair pair;
	private String myId;
	private Store localStore;
	Queue<Message> incomingMessages;
	Queue<Message> outgoingMessages;
	private int updateTimer;
	private int followTimer;
	private int reconnectionTime;

	Map<String, RepastEdge<Object>> followed;
	Map<String, RepastEdge<Object>> blocked;

	private EventAnalysis ea;
	private int sentMessages;
	private int sentMessagesStep;
	private int newsExchanged;
	private int generatedEvents;

	private Parameters params;
	private int algorithm;

	public enum PartType {
		READY, DISCONNECTED
	}

	private PartType type;

	public Participant(ContinuousSpace<Object> space, Grid<Object> grid, EventAnalysis ea) throws Exception {
		this.space = space;
		this.grid = grid;
		this.params = RunEnvironment.getInstance().getParameters();
		this.algorithm = (int) params.getValue("algorithm");
		this.ea = ea;

		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
			keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
			pair = keyGen.generateKeyPair();
			myId = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

			this.localStore = new Store();
			localStore.add(new EventLog(myId, this, ea));
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.incomingMessages = new LinkedList<>();
		this.outgoingMessages = new LinkedList<>();
		this.updateTimer = RandomHelper.nextIntFromTo(0, getUpdateFreq());

		this.followed = new HashMap<>();
		this.blocked = new HashMap<>();

		this.sentMessages = 0;
		this.newsExchanged = 0;
		this.generatedEvents = 0;

		this.setType(PartType.READY);
	}

	@SuppressWarnings("unchecked")
	@ScheduledMethod(start = 1, interval = 0)
	public void init() throws Exception {
		context = (Context<Object>) ContextUtils.getContext(this);
		connNet = (Network<Object>) context.getProjection("connections network");
		socialNet = (Network<Object>) context.getProjection("social network");
		blockedNet = (Network<Object>) context.getProjection("blocked network");
		if (algorithm == 2) {
			int follow = getInitFollow();
			List<Participant> participants = new ArrayList<>();
			context.getObjects(Participant.class).forEach((p) -> {
				if (p != this) {
					participants.add((Participant) p);
				}
			});
			for (int i = 0; i < follow; i++) {
				Participant p = getRandomParticipant(participants);
				participants.remove(p);
				String id = p.getId();
				RepastEdge<Object> edge = new RepastEdge<>(this, p, true);
				socialNet.addEdge(edge);
				followed.put(id, edge);
				Map<String, String> content = new HashMap<>();
				content.put("follow", id);
				try {
					localStore.get(myId).append(content, pair.getPrivate());
					generatedEvents++;
				} catch (WrongLogException e) {
					System.out.println("Wrong Log Exception");
				}
			}
		}
	}

	@ScheduledMethod(start = 2, interval = 1, priority = 999)
	public void step() throws Exception {
		sentMessagesStep = 0;

		// enter DISCONNECTED state with a given probability
		if (this.type != PartType.DISCONNECTED && RandomHelper.nextDoubleFromTo(0, 1) <= getDisconnectionProb()) {
			this.setType(PartType.DISCONNECTED);
			reconnectionTime = (int) this.getTick() + RandomHelper.nextIntFromTo(0, getDisconnectionTime());
		}

		// recover from DISCONNECTED state if reconnection time has passed
		if (this.type == PartType.DISCONNECTED && (int) this.getTick() > reconnectionTime) {
			this.setType(PartType.READY);
		}

		// create a new local event with a given probability
		if (RandomHelper.nextDoubleFromTo(0, 1) <= getEventProb()) {
			Map<String, String> content = new HashMap<>();
			content.put("content", Integer.toHexString(RandomHelper.nextIntFromTo(0, 1000)));
			try {
				localStore.get(myId).append(content, pair.getPrivate());
				generatedEvents++;
			} catch (WrongLogException e) {
				System.out.println("Wrong Log Exception");
			}
		}

		if (this.type != PartType.DISCONNECTED) {
			// start the update protocol with a given probability
			if (updateTimer <= getTick()) {
				updateTimer = (int) getTick() + getUpdateFreq();
				Participant p = null;
				if (getNeighbors() > 0) {
					List<Participant> participants = getNearestNeighbors(getNeighbors());
					p = getRandomParticipant(participants);
				} else {
					p = getRandomParticipant();
				}
				RepastEdge<Object> edge = new RepastEdge<>(this, p, true);
				connNet.addEdge(edge);

				Map<String, Object> content = new HashMap<>();
				Message m;
				if (algorithm == 1) {
					content.put("ids", this.localStore.getIds());
					m = new Message(MsgType.A, this, p, content, edge);
				} else {
					followed.forEach((id, ed) -> {
						try {
							localStore.add(new EventLog(id, this, ea));
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
					blocked.forEach((id, ed) -> {
						try {
							localStore.remove(id);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
					Set<SimpleEntry<String, Integer>> frontier = localStore.getFrontier(localStore.getIds());
					content.put("frontier", frontier);
					m = new Message(MsgType.B, this, p, content, edge);
				}
				this.outgoingMessages.add(m);
			}

			// update followed participants
			if (followTimer <= getTick()) {
				followTimer = (int) getTick() + getFollowFreq();
				updateFollowed();
			}

			// send messages
			Iterator<Message> it = this.outgoingMessages.iterator();
			while (it.hasNext() && GossipBuilder.bandwidth > 0) {
				Message m = it.next();
				if (m.getDeliveryTime() <= getTick()) {
					Participant p = m.getDst();
					if (p.type != PartType.DISCONNECTED) {
						it.remove();
						p.incomingMessages.add(m);
						GossipBuilder.bandwidth--;
						sentMessagesStep++;
						sentMessages++;
						newsExchanged = getSize(m.getContent());
					}
				}
			}
		}
	}

	public void updateFollowed() throws Exception {
		if (algorithm == 2) {
			// follow a new participant, from the ones known by followed participants
			Set<String> ids = localStore.getFollowed();
			List<String> newIds = new ArrayList<>();
			ids.forEach((id) -> {
				if (id != myId && !blocked.containsKey(id) && !followed.containsKey(id)) {
					newIds.add(id);
				}
			});
			if (newIds.size() > 0) {
				Collections.shuffle(newIds);
				String id = newIds.get(0);
				Participant p = getParticipant(id);
				RepastEdge<Object> edge = new RepastEdge<>(this, p, true);
				socialNet.addEdge(edge);
				followed.put(id, edge);
				Map<String, String> content1 = new HashMap<>();
				content1.put("follow", id);
				try {
					localStore.get(myId).append(content1, pair.getPrivate());
					generatedEvents++;
				} catch (WrongLogException e) {
					System.out.println("Wrong Log Exception");
				}

				// unfollow a random participant
				if (followed.size() > 1) {
					int index = RandomHelper.nextIntFromTo(0, followed.size() - 1);
					Iterator<String> iter = followed.keySet().iterator();
					for (int i = 0; i < index - 1; i++) {
						iter.next();
					}
					String id1 = iter.next();
					socialNet.removeEdge(followed.get(id1));
					iter.remove();
					Map<String, String> content = new HashMap<>();
					content.put("unfollow", id1);
					try {
						localStore.get(myId).append(content, pair.getPrivate());
						generatedEvents++;
					} catch (WrongLogException e) {
						System.out.println("Wrong Log Exception");
					}
				}
			}
		}
	}

	@ScheduledMethod(start = 51, interval = 50)
	public void updateBlocked() throws Exception {
		if (algorithm == 2) {
			// simulate blocking of participants
			if (getBlockProbability() > RandomHelper.nextDoubleFromTo(0, 1)) {
				block(getRandomParticipant());
			}

			// check if other participant must be blocked based on other logs
			localStore.getBlocked().forEach((id, count) -> {
				if (count >= getBlockFactor() && !blocked.containsKey(id)) {
					try {
						block(getParticipant(id));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void onReceive() throws Exception {
		Message m = incomingMessages.poll();
		if (m != null) {
			if (this.type != PartType.DISCONNECTED) {
				switch (m.getType()) {
				case A:
					updateI(m);
					break;
				case B:
					updateII(m);
					break;
				case C:
					updateIII(m);
					break;
				case D:
					updateIV(m);
					break;
				default:
					break;
				}
			} else {
				socialNet.removeEdge(m.getEdge());
			}
		}
	}

	private void updateI(Message msg) {
		if (algorithm == 1) {
			Set<String> idsA = (Set<String>) msg.getContent().get("ids");
			Set<String> ids = Store.idsDifference(idsA, this.localStore.getIds());
			ids.forEach((id) -> {
				try {
					localStore.add(new EventLog(id, this, ea));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Set<SimpleEntry<String, Integer>> frontier = localStore.getFrontier(localStore.getIds());

		RepastEdge<Object> edge = msg.getEdge();
		RepastEdge<Object> newEdge = new RepastEdge<>(this, msg.getSrc(), true);
		connNet.removeEdge(edge);
		connNet.addEdge(newEdge);

		Map<String, Object> content = new HashMap<>();
		if (algorithm == 1) {
			content.put("ids", this.localStore.getIds());
		}
		content.put("frontier", frontier);
		Message m = new Message(MsgType.B, this, msg.getSrc(), content, newEdge);
		this.outgoingMessages.add(m);
	}

	private void updateII(Message msg) {
		if (algorithm == 1) {
			Set<String> idsA = (Set<String>) msg.getContent().get("ids");
			Set<String> ids = Store.idsDifference(idsA, this.localStore.getIds());
			ids.forEach((id) -> {
				try {
					localStore.add(new EventLog(id, this, ea));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		Set<SimpleEntry<String, Integer>> frontier = localStore.getFrontier(localStore.getIds());
		Set<SimpleEntry<String, Integer>> frontierA = (Set<SimpleEntry<String, Integer>>) msg.getContent()
				.get("frontier");
		Map<String, List<Event>> news = localStore.since(frontierA);

		RepastEdge<Object> edge = msg.getEdge();
		RepastEdge<Object> newEdge = new RepastEdge<>(this, msg.getSrc(), true);
		connNet.removeEdge(edge);
		connNet.addEdge(newEdge);

		Map<String, Object> content = new HashMap<>();
		content.put("frontier", frontier);
		content.put("news", news);
		Message m = new Message(MsgType.C, this, msg.getSrc(), content, newEdge);
		this.outgoingMessages.add(m);
	}

	private void updateIII(Message msg) throws Exception {
		Set<SimpleEntry<String, Integer>> frontierA = (Set<SimpleEntry<String, Integer>>) msg.getContent()
				.get("frontier");

		Map<String, List<Event>> news = localStore.since(frontierA);
		Map<String, List<Event>> newsA = (Map<String, List<Event>>) msg.getContent().get("news");

		try {
			localStore.update(newsA);
		} catch (WrongLogException e) {
			System.out.println("Update discarded: wrong log");
		} catch (ChainErrorException e) {
			// System.out.println("Update discarded: chain error");
		} catch (NullPointerException e) {
			// System.out.println("Null Pointer");
		}

		RepastEdge<Object> edge = msg.getEdge();
		RepastEdge<Object> newEdge = new RepastEdge<>(this, msg.getSrc(), true);
		connNet.removeEdge(edge);
		connNet.addEdge(newEdge);

		Map<String, Object> content = new HashMap<>();
		content.put("news", news);
		Message m = new Message(MsgType.D, this, msg.getSrc(), content, newEdge);
		this.outgoingMessages.add(m);
	}

	private void updateIV(Message msg) throws Exception {
		Map<String, List<Event>> newsA = (Map<String, List<Event>>) msg.getContent().get("news");

		try {
			localStore.update(newsA);
		} catch (WrongLogException e) {
			System.out.println("Update discarded: wrong log");
		} catch (ChainErrorException e) {
			// System.out.println("Update discarded: chain error");
		} catch (NullPointerException e) {
			// System.out.println("Null Pointer");
		}

		RepastEdge<Object> edge = msg.getEdge();
		connNet.removeEdge(edge);
	}

	public PublicKey getPublicKey() {
		return pair.getPublic();
	}

	public String getId() {
		return myId;
	}

	public int getSentMessages() {
		return this.sentMessages;
	}

	public int getSentMessagesStep() {
		return this.sentMessagesStep;
	}
	
	public int getNewsExchanged() {
		return this.newsExchanged;
	}

	public int getGeneratedEvents() {
		return this.generatedEvents;
	}

	public int getTotalEvents() {
		return this.localStore.getTotalEvents();
	}

	public PartType getType() {
		return type;
	}

	public void setType(PartType type) {
		this.type = type;
	}

	private Participant getRandomParticipant() {
		IndexedIterable<Object> participants = context.getObjects(Participant.class);
		Participant p = this;
		if (participants.size() > 1) {
			while (p == this) {
				int idx = RandomHelper.nextIntFromTo(0, participants.size() - 1);
				p = (Participant) participants.get(idx);
			}
		} else {
			p = null;
		}
		return p;
	}

	private Participant getRandomParticipant(List<Participant> participants) {
		return (Participant) participants.get(RandomHelper.nextIntFromTo(0, participants.size() - 1));
	}

	/*
	 * Find a list of 'size' nearest neighbors
	 */
	private List<Participant> getNearestNeighbors(int size) {
		List<Participant> neighbors = new ArrayList<>();
		List<Participant> participants = new ArrayList<>();
		context.getObjects(Participant.class).forEach((p) -> {
			if (p != this) {
				participants.add((Participant) p);
			}
		});
		if (size > participants.size()) {
			size = participants.size();
		}
		while (neighbors.size() < size) {
			Iterator<Participant> it = participants.iterator();
			Participant nearest = (Participant) it.next();
			while (it.hasNext()) {
				Participant p = it.next();
				if (getDistance(this, p) < getDistance(this, nearest)) {
					nearest = p;
				}
			}
			neighbors.add(nearest);
			participants.remove(nearest);
		}
		return neighbors;
	}

	private double getTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}

	private double getEventProb() {
		return (double) params.getValue("event_probability");
	}

	private int getUpdateFreq() {
		return (int) params.getValue("update_frequency");
	}

	private int getFollowFreq() {
		return (int) params.getValue("follow_frequency");
	}

	private int getNeighbors() {
		return (int) params.getValue("neighbors");
	}

	private int getInitFollow() {
		return (int) params.getValue("init_follow");
	}

	private double getBlockProbability() {
		return (double) params.getValue("block_probability");
	}

	private int getBlockFactor() {
		return (int) params.getValue("block_factor");
	}

	private double getDisconnectionProb() {
		return (double) params.getValue("disconnection_probability");
	}

	private int getDisconnectionTime() {
		return (int) params.getValue("disconnection_time");
	}

	private int getDistance(Participant src, Participant dst) {
		NdPoint pt1 = space.getLocation(src);
		NdPoint pt2 = space.getLocation(dst);
		return (int) space.getDistance(pt1, pt2);
	}

	private Participant getParticipant(String id) {
		IndexedIterable<Object> participants = context.getObjects(Participant.class);
		Participant participant = null;
		Iterator<Object> it = participants.iterator();
		while (it.hasNext() && participant == null) {
			Participant p = (Participant) it.next();
			if (p.getId().equals(id)) {
				participant = p;
			}
		}
		return participant;
	}

	private void block(Participant p) throws Exception {
		// remove the participant from the followed ones
		Iterator<String> iter = followed.keySet().iterator();
		while (iter.hasNext()) {
			String id = iter.next();
			if (id.equals(p.getId())) {
				socialNet.removeEdge(followed.get(id));
				iter.remove();
				Map<String, String> content = new HashMap<>();
				content.put("unfollow", id);
				try {
					localStore.get(myId).append(content, pair.getPrivate());
					generatedEvents++;
				} catch (WrongLogException e) {
					System.out.println("Wrong Log Exception");
				}
			}
		}

		// add the participant to the blocked ones
		RepastEdge<Object> edge = new RepastEdge<>(this, p, true);
		blockedNet.addEdge(edge);
		blocked.put(p.getId(), edge);
		Map<String, String> content = new HashMap<>();
		content.put("block", p.getId());
		try {
			localStore.get(myId).append(content, pair.getPrivate());
			generatedEvents++;
		} catch (WrongLogException e) {
			System.out.println("Wrong Log Exception");
		}
	}

	private int getSize(Map<String, Object> map) {
		AtomicInteger size = new AtomicInteger();
		map.forEach((k, v) -> {
			if (v instanceof Map<?, ?>) {
				size.addAndGet(getSizeEvents((Map<String, List<Event>>) v));
			}
		});
		return size.get();
	}

	private int getSizeEvents(Map<String, List<Event>> map) {
		AtomicInteger size = new AtomicInteger();
		map.forEach((k, v) -> {
			v.forEach((i) -> {
				size.incrementAndGet();
			});
		});
		return size.get();
	}
}
