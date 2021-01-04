package ssbGossip;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import exceptions.BrokenSignature;
import interfaces.Node;
import interfaces.Packet;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.continuous.ContinuousSpace;
import ssbGossip.Helpers.ContextHelper;
import ssbGossip.Helpers.Logger;
import ssbGossip.Helpers.NodeFinder;
import ssbGossip.Helpers.ParamHelper;

public class OpenGossip implements Node {
	protected Store store;
	protected PrivateKey privateKey;
	protected PublicKey publicKey;
	protected Cipher cipher;
	protected CustomEdge<?> myEdge;
	protected boolean failed = false;
	protected boolean insertedAfter;
	protected double tickCreation;

	public OpenGossip(boolean insertedAfter) {
		this.insertedAfter = insertedAfter;
		this.store = new Store();
		ISchedule s = RunEnvironment.getInstance().getCurrentSchedule();
		this.tickCreation = s.getTickCount();
		// schedule events generation
		s.schedule(ScheduleParameters.createPoissonProbabilityRepeating(s.getTickCount() + ParamHelper.meanGeneration,
				ParamHelper.meanGeneration,
				ScheduleParameters.RANDOM_PRIORITY), this, "addEvent", new EventContent());

		s.schedule(ScheduleParameters.createPoissonProbabilityRepeating(ParamHelper.syncLambda,
				ParamHelper.syncLambda, ScheduleParameters.RANDOM_PRIORITY), this, "synchronizeStores");
		

		try {
			// generate keys
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(1024);
			KeyPair pair = keyGen.generateKeyPair();
			this.privateKey = pair.getPrivate();
			this.publicKey = pair.getPublic();
			cipher = Cipher.getInstance("RSA");
			NodeFinder.addNode(this);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.err.println("The selected encryption algorithm is not available");
			System.exit(1);
		}
	}

	@Override
	public void onReceive(Packet p, CustomEdge<Node> edge) {
		if (!failed) {
			if (p instanceof Event) {
				Event e = (Event) p;
				PublicKey pub = e.getId();
				byte[] signature = e.getSignature();
				byte[] barr = ByteBuffer.allocate(Long.BYTES).putLong(e.hashCode()).array();
				try {
					cipher.init(Cipher.DECRYPT_MODE, pub);
					// signature verification
					if (!Arrays.equals(cipher.doFinal(signature), barr)) {
						throw new BrokenSignature();
					}
					// retrieve the next expected index, if the log doesn't exist the expected index
					// is 1
					int next = this.store.getLog(pub) == null ? 1 : this.store.getLog(pub).getLatest().getIndex() + 1;
					if (e.getIndex() == next) {
						// events correctly received and stored are logged in a csv
						Logger.recordEvent(e, this);
						this.store.update(e);
					}
				} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | BrokenSignature e1) {
					// TODO Invalid signature
					e1.printStackTrace();
				}
			} else if (p instanceof FrontierMessage) {
				// if I haven't already asked for news, I do it
				if (!edge.asked(this)) {
					edge.take(new FrontierMessage(this.store.getFrontier(this.store.getIDs()), this), this);
				}
				FrontierMessage fm = (FrontierMessage) p;
				// for each log in my store
				for (PublicKey pk : store.getIDs()) {
					// if the node who asked me for news doesn't know about a node I know I add it
					// to the list
					if (!fm.getFrontier().contains(pk)) {
						fm.getFrontier().add(new FrontierTuple(pk, 0));
					}
				}
				// for every event that is not present in the store of the other node
				for (Event e : store.getEventsSince(fm.getFrontier())) {
					edge.take(e, this);
				}
				/*
				 * when I am done I send an end message to close connection (which is closed
				 * after the other node sends an end
				 */
				edge.take(new End(), this);

			} else if (p instanceof End) {
				edge.end();
			}
		}
	}

	@Override
	public PublicKey getPublicKey() {
		return publicKey;
	}

	@Override
	// @ScheduledMethod(start = 2000, interval = 50, pick = 30)
	// method which pick a node and asks for news
	public void synchronizeStores() {
		if (!failed) {
			/*
			 * if I am still receiving/sending from my previous request I wait the next
			 * interval, requests from other nodes doesn't count
			 */
			if (myEdge == null || ContextHelper.graph.getEdge(myEdge.getSource(), myEdge.getTarget()) == null) {
				Node node;
				do {
					node = getRandomNode();
				} while (ContextHelper.graph.getEdge(this, node) != null
						|| ContextHelper.graph.getEdge(node, this) != null || node.isFailed());

				ContinuousSpace<Object> space = ContextHelper.space;
				CustomEdge<?> edge = (CustomEdge<?>) ContextHelper.graph.addEdge(this, node,
						/* reach every location in at most 1.5 ms*/ 0.03 * space.getDistance(space.getLocation(this), space.getLocation(node)));
				myEdge = edge;
				updateStore();
				edge.take(new FrontierMessage(this.store.getFrontier(this.store.getIDs()), this), this);
			}
		}
	}

	protected void updateStore() {
		// for compatibility with Transitive Interest
	}

	// method which generates new events
	public void addEvent(EventContent content) {
		if (!failed) {
			Log me = this.getOrCreateLog();
			Event last = me.getLatest();
			int size = new Random().nextInt(1450)+50; //Packet size between 50 and 1500 bytes
			Event e = new Event(this.publicKey, last, last != null ? (last.getIndex() + 1) : 1, content, size);

			try {
				cipher.init(Cipher.ENCRYPT_MODE, this.privateKey);
				byte[] barr = ByteBuffer.allocate(Long.BYTES).putLong(e.hashCode()).array();
				e.sign(cipher.doFinal(barr));
				me.add(e);
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e1) {
				e1.printStackTrace();
			}
		}
	}

	protected Log getOrCreateLog() {
		Log me = this.store.getLog(this.publicKey);

		if (me == null) {
			me = new Log(this.publicKey);
			this.store.addLog(me);
		}

		return me;
	}

	protected Node getRandomNode() {
		Node node = null;

		do
			node = (Node) ContextHelper.context.getRandomObjects(Node.class, 1).iterator().next();
		while (node == this);

		return node;
	}

	@Override
	public void fail() {
		System.out.println("Node failed");
		failed = true;
	}

	@Override
	public boolean isFailed() {
		return failed;
	}

	public boolean isInsertedAfter() {
		return insertedAfter;
	}

	public double getTickCreation() {
		return tickCreation;
	}
	
}