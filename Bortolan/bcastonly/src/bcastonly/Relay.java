package bcastonly;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javafx.util.Pair;
import analysis.PerturbationAnalysis;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.IndexedIterable;

/**
 * Represent a node in the broadcast-only network
 * 
 * @author bortolan cosimo
 */
public class Relay {
	private int id;
	private int group;
	private int vc;

	Queue<Perturbation> incomingMessages;
	Queue<Message> outgoingMessages;
	private double lastMod;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private Parameters params;
	private int reconnectionTime;

	// data structures for analysis
	private PerturbationAnalysis pa;
	private int workload;
	private int sentMessages;
	private int sentMessagesStep;
	private int sentPerturbations;
	private int sentARQRequests;

	// data structures for relay I
	private Map<Integer, Integer> frontier;
	private int discardedPerturbation;

	// data structures for relay II
	private Set<Perturbation> bag;

	// data structures for relay III
	private Map<Integer, LinkedList<Perturbation>> logs;
	private Set<Perturbation> lostPerturbations;
	private int arqTime;
	private int arqCounter;
	private KeyPair pair;

	public enum RelayType {
		QUIETE, SENDER, RELAY, ARQ_REQ, DISCONNECTED
	}

	private RelayType type;

	public Relay(ContinuousSpace<Object> space, Grid<Object> grid,
			PerturbationAnalysis pa, int group) {
		Context<Object> context = (Context<Object>) ContextUtils
				.getContext(this);
		this.params = RunEnvironment.getInstance().getParameters();
		this.space = space;
		this.grid = grid;
		this.id = this.hashCode();
		this.vc = 0;
		this.pa = pa;
		this.group = group;

		this.incomingMessages = new LinkedList<>();
		this.outgoingMessages = new LinkedList<>();

		this.frontier = new HashMap<>();
		this.bag = new HashSet<>();
		this.logs = new HashMap<>();
		this.lostPerturbations = new HashSet<>();
		this.arqTime = RandomHelper.nextIntFromTo(0, 100);

		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(512);
			pair = keyPairGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		this.setType(RelayType.QUIETE);
	}

	@ScheduledMethod(start = 1, interval = 1, priority = 999)
	public void step() throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, IOException, ClassNotFoundException {
		workload = getWorkload();
		sentMessagesStep = 0;

		// enter DISCONNECTED state with a given probability
		if (this.type != RelayType.DISCONNECTED
				&& RandomHelper.nextDoubleFromTo(0, 1) <= getDisconnectionProb()) {
			this.setType(RelayType.DISCONNECTED);
			reconnectionTime = (int) this.getTick()
					+ RandomHelper.nextIntFromTo(0, getDisconnectionTime());
		}

		// recover from DISCONNECTED state if reconnection time has passed
		if (this.type == RelayType.DISCONNECTED
				&& (int) this.getTick() > reconnectionTime) {
			this.setType(RelayType.QUIETE);
		}

		// start a perturbation with a given probability
		if (RandomHelper.nextDoubleFromTo(0, 1) <= getSendProb()) {
			int msg = RandomHelper.nextIntFromTo(0, 1000);
			Perturbation p = new Perturbation(id, vc++, msg);
			setType(RelayType.SENDER);
			lastMod = getTick() + 10;
			pa.sent(p);
			broadcast(p);
			sentPerturbations++;
			workload--;
		}

		// send a P2P message with a given probability
		if (RandomHelper.nextDoubleFromTo(0, 1) <= getP2PProb()
				&& frontier.size() > 0) {
			int msg = RandomHelper.nextIntFromTo(0, 1000);
			int dest = (int) frontier.keySet().toArray()[RandomHelper
					.nextIntFromTo(0, frontier.size() - 1)];
			Perturbation p = new Perturbation(id, vc++,
					new Pair<Integer, Integer>(dest, msg));
			setType(RelayType.SENDER);
			lastMod = getTick() + 10;
			pa.sent(p);
			broadcast(p);
			sentPerturbations++;
			workload--;
		}

		// send a multicast message with a given probability
		if (RandomHelper.nextDoubleFromTo(0, 1) <= getP2MProb()) {
			int msg = RandomHelper.nextIntFromTo(0, 1000);
			int grp = RandomHelper.nextIntFromTo(-10, 0);
			Perturbation p = new Perturbation(id, vc++,
					new Pair<Integer, Integer>(grp, msg));
			setType(RelayType.SENDER);
			lastMod = getTick() + 10;
			pa.sent(p);
			broadcast(p);
			sentPerturbations++;
			workload--;
		}

		// send an enncripted message with a given probability
		if (RandomHelper.nextDoubleFromTo(0, 1) <= getP2SProb()
				&& frontier.size() > 0) {
			int msg = RandomHelper.nextIntFromTo(0, 10);
			int dest = (int) frontier.keySet().toArray()[RandomHelper
					.nextIntFromTo(0, frontier.size() - 1)];

			//encript the message
			Relay destRelay = getRelay(dest);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, destRelay.getPublicKey());
			String encrMsg = Base64.getEncoder().encodeToString(
					cipher.doFinal(("" + msg).getBytes()));
			Perturbation p = new Perturbation(id, vc++, encrMsg);

			setType(RelayType.SENDER);
			lastMod = getTick() + 10;
			pa.sent(p);
			broadcast(p);
			sentPerturbations++;
			workload--;
		}

		// send ARQ request
		if (getRelayType() == 3 && arqTime < this.getTick()) {
			arqTime = (int) this.getTick() + 100;
			frontier.keySet().forEach((k) -> {
				Perturbation p = new Perturbation(k, -1, frontier.get(k));
				broadcast(p);
				sentPerturbations++;
				sentARQRequests++;
			});
		}

		// process received messages
		if (!this.incomingMessages.isEmpty()) {
			if (this.type != RelayType.SENDER
					&& this.type != RelayType.DISCONNECTED) {
				lastMod = getTick();
				setType(RelayType.RELAY);
			}
			while (!this.incomingMessages.isEmpty() && workload > 0) {
				Perturbation p = this.incomingMessages.poll();

				// discard ARQ requests if not relay III
				if (this.getRelayType() != 3 && p.getRef() == -1) {
					p = null;
				}

				// simulate network loss with a given probability
				if (RandomHelper.nextDoubleFromTo(0, 1) <= getLossProb()) {
					p = null;
				}

				// process message
				if (p != null) {
					switch (getRelayType()) {
					case 1:
						this.receiveI(p);
						break;
					case 2:
						this.receiveII(p);
						break;
					case 3:
						if (p.getRef() != -1) {
							this.receiveIII(p);
						} else {
							if (this.type != RelayType.SENDER
									&& this.type != RelayType.DISCONNECTED) {
								lastMod = getTick();
								setType(RelayType.ARQ_REQ);
							}
							this.receiveARQ(p);
						}
						break;
					default:
						break;
					}
					workload--;
				}
			}
		}

		// if not DISCONNECTED send messages
		if (this.type != RelayType.DISCONNECTED) {
			while (!this.outgoingMessages.isEmpty()
					&& BcastOnlyBuilder.bandwidth > 0) {
				Message m = this.outgoingMessages.poll();
				if (m != null) {
					Perturbation p = m.getPerturbation();
					Relay r = m.getRecipient();
					if (r.getType() != RelayType.DISCONNECTED) {
						r.incomingMessages.add(p);
						sentMessages++;
						sentMessagesStep++;
						BcastOnlyBuilder.bandwidth--;
					}
					if (p.getRef() != -1) {
						pa.addMessages(p, 1);
					}
				}
			}
		}

		// move by a given distance
		int movingFactor = getMovingFactor();
		if (movingFactor > 0) {
			List<GridCell<Relay>> gridCells = getNeighborhood(5);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
			GridCell<Relay> firstCell = gridCells.get(0);
			if (firstCell.size() == 0) {
				moveTowards(firstCell.getPoint(), RandomHelper.nextIntFromTo(0, movingFactor));
			}
		}

		// reset state
		if (this.type != RelayType.DISCONNECTED && getTick() > lastMod + 5) {
			this.type = RelayType.QUIETE;
		}

	}

	/*
	 * Delete all edges from visualization networks
	 */
	@ScheduledMethod(start = 0, interval = 50)
	public void clearNetworks() {
		Context<Object> context = (Context<Object>) ContextUtils
				.getContext(this);
		Network<Object> p2pNet = (Network<Object>) context
				.getProjection("p2p network");
		Network<Object> p2mNet = (Network<Object>) context
				.getProjection("p2m network");
		Network<Object> p2sNet = (Network<Object>) context
				.getProjection("p2s network");
		p2pNet.removeEdges();
		p2mNet.removeEdges();
		p2sNet.removeEdges();
	}

	/*
	 * Broadcast a given perturbation to neighbours. This method only adds the
	 * perturbation to the out queue of this relay.
	 */
	private void broadcast(Perturbation p) {
		List<GridCell<Relay>> gridCells = getNeighborhood(getBroadcastDomain());
		for (GridCell<Relay> gridCell : gridCells) {
			gridCell.items().forEach((r) -> {
				this.outgoingMessages.add(new Message(r, p));
			});
		}
	}

	/*
	 * Relay I implementation: static network and zero jitter
	 */
	public void receiveI(Perturbation p) {
		// a new node will accept the first message from each other node
		if (frontier.get(p.getSrc()) == null) {
			frontier.putIfAbsent(p.getSrc(), p.getRef());
		}
		if (p.getRef() == frontier.get(p.getSrc())) {
			frontier.put(p.getSrc(), p.getRef() + 1);
			pa.received(p, this);
			broadcast(p);
		} else if (p.getRef() > frontier.get(p.getSrc())) {
			this.discardedPerturbation++;
		}
	}

	/*
	 * Relay II implementation: dynamic network and variable delays
	 */
	public void receiveII(Perturbation p) {
		// a new node will accept the first message from each other node
		if (frontier.get(p.getSrc()) == null) {
			frontier.putIfAbsent(p.getSrc(), p.getRef());
		}
		if (p.getRef() >= frontier.get(p.getSrc()) && !bag.contains(p)) {
			bag.add(p);
			boolean found;
			do {
				found = false;
				Iterator<Perturbation> it = bag.iterator();
				while (it.hasNext()) {
					Perturbation q = it.next();
					if (q.getRef() == frontier.get(q.getSrc())) {
						found = true;
						frontier.put(q.getSrc(), q.getRef() + 1);
						pa.received(q, this);
						broadcast(q);
						it.remove();
					}
				}
			} while (found);
		}
	}

	/*
	 * Relay III implementation: arbitrary network dynamics, delay, losses
	 */
	public void receiveIII(Perturbation p) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, ClassNotFoundException, IOException {

		// instantiate log for newly-dicoverd relays
		if (!logs.containsKey(p.getSrc())) {
			logs.put(p.getSrc(), new LinkedList<>());
		}

		// a new node expect the initial message of other nodes
		if (frontier.get(p.getSrc()) == null) {
			frontier.putIfAbsent(p.getSrc(), 0);
		}

		// add perturbation to log if expected
		if (p.getRef() == frontier.get(p.getSrc())) {
			frontier.put(p.getSrc(), p.getRef() + 1);
			broadcast(p);
			logs.get(p.getSrc()).add(p);
			lostPerturbations.remove(p);
			pa.received(p, this);

			Context<Object> context = (Context<Object>) ContextUtils
					.getContext(this);

			// check if the message was sent to me
			if (p.getVal() instanceof Pair<?, ?>) {
				Pair<Integer, Integer> message = (Pair<Integer, Integer>) p
						.getVal();

				// p2p message
				if (message.getKey() == this.id) {
					Network<Object> p2pNet = (Network<Object>) context
							.getProjection("p2p network");
					IndexedIterable<Object> relays = context
							.getObjects(Relay.class);
					Relay sender = getRelay(p.getSrc());
					if (sender != null) {
						p2pNet.addEdge(sender, this);
					}
				}

				// p2m message
				if (message.getKey() == this.group) {
					Network<Object> p2mNet = (Network<Object>) context
							.getProjection("p2m network");
					IndexedIterable<Object> relays = context
							.getObjects(Relay.class);
					Relay sender = getRelay(p.getSrc());
					if (sender != null) {
						p2mNet.addEdge(sender, this);
					}
				}
			}

			// check if the message was encripted with my key
			if (p.getVal() instanceof String) {
				String encrMsg = (String) p.getVal();
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
				try {
					String msg = new String(cipher.doFinal(Base64.getDecoder()
							.decode(encrMsg)));
					Network<Object> p2sNet = (Network<Object>) context
							.getProjection("p2s network");
					IndexedIterable<Object> relays = context
							.getObjects(Relay.class);
					Relay sender = getRelay(p.getSrc());
					if (sender != null) {
						p2sNet.addEdge(sender, this);
					}
				} catch (BadPaddingException e) {
				}
			}
		} else if (p.getRef() > frontier.get(p.getSrc())) {
			lostPerturbations.add(p);
		}
	}

	/*
	 * Process received ARQ request: brodcast reply only if required
	 * perturbation is known.
	 */
	private void receiveARQ(Perturbation p) {
		List<Perturbation> log = logs.get(p.getSrc());
		if (log != null) {
			Iterator<Perturbation> it = log.iterator();
			while (it.hasNext()) {
				Perturbation q = it.next();
				if (q.getRef() >= (int) p.getVal()) {
					broadcast(q);
					arqCounter++;
				}
			}
		}
	}

	/*
	 * Use the GridCellNgh class to create GridCells for the surrounding
	 * neighborhood, with dimensions equal to dim
	 */
	private List<GridCell<Relay>> getNeighborhood(int dim) {
		GridPoint pt = grid.getLocation(this);
		GridCellNgh<Relay> nghCreator = new GridCellNgh<Relay>(grid, pt,
				Relay.class, dim, dim);
		return nghCreator.getNeighborhood(false);
	}

	/*
	 * Move in the direction of the given point for a given distance
	 */
	private void moveTowards(GridPoint pt, int distance) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
				otherPoint);
		space.moveByVector(this, distance, angle, 0);
		myPoint = space.getLocation(this);
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
	}

	public RelayType getType() {
		return this.type;
	}

	public void setType(RelayType type) {
		this.type = type;
	}

	private double getSendProb() {
		return (double) params.getValue("send_probability");
	}

	private double getP2PProb() {
		return (double) params.getValue("send_p2p_probability");
	}

	private double getP2MProb() {
		return (double) params.getValue("send_p2m_probability");
	}

	private double getP2SProb() {
		return (double) params.getValue("send_p2s_probability");
	}

	private int getMovingFactor() {
		return (int) params.getValue("moving_factor");
	}

	private int getBroadcastDomain() {
		return (int) params.getValue("broadcast_domain");
	}

	private int getRelayType() {
		return (int) params.getValue("relay_type");
	}

	private int getWorkload() {
		return (int) params.getValue("workload");
	}

	private double getLossProb() {
		return (double) params.getValue("loss_probability");
	}

	private double getDisconnectionProb() {
		return (double) params.getValue("disconnection_probability");
	}

	private int getDisconnectionTime() {
		return (int) params.getValue("disconnection_time");
	}

	public int getDiscardedPerturbations() {
		return this.discardedPerturbation;
	}

	public int getBagSize() {
		return this.bag.size();
	}

	public int getOutgoingSize() {
		return this.outgoingMessages.size();
	}

	public int getLostPerturbations() {
		return this.lostPerturbations.size();
	}

	public int getSentMessages() {
		return this.sentMessages;
	}

	public int getSentMessagesStep() {
		return this.sentMessagesStep;
	}

	public int getSentPerturbations() {
		return this.sentPerturbations;
	}

	public int getSentARQRequests() {
		return this.sentARQRequests;
	}

	public int getRelayLoad() {
		return getWorkload() - this.workload;
	}

	public int getAQRReplies() {
		return this.arqCounter;
	}

	public int receivedPerturbations() {
		int sum = 0;
		Iterator<Integer> it = frontier.keySet().iterator();
		while (it.hasNext()) {
			sum += frontier.get(it.next());
		}
		return sum;
	}

	public int getId() {
		return this.id;
	}

	public PublicKey getPublicKey() {
		return pair.getPublic();
	}

	private double getTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}

	private Relay getRelay(int id) {
		Context<Object> context = (Context<Object>) ContextUtils
				.getContext(this);
		IndexedIterable<Object> relays = context.getObjects(Relay.class);
		Relay relay = null;
		Iterator<Object> it = relays.iterator();
		while (it.hasNext() && relay == null) {
			Relay r = (Relay) it.next();
			if (r.getId() == id) {
				relay = r;
			}
		}
		return relay;
	}

}
