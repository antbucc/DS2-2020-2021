package ssbGossip;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import exceptions.BrokenSignature;
import interfaces.Node;
import interfaces.Packet;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import ssbGossip.Helpers.ContextHelper;
import ssbGossip.Helpers.Logger;
import ssbGossip.Helpers.NodeFinder;
import ssbGossip.Helpers.ParamHelper;
import ssbGossip.InterestOperation.Operation;

public class TransitiveInterest extends OpenGossip {

	protected HashMap<PublicKey, Double> followedFrom = new HashMap<>();
	
	public TransitiveInterest(boolean insertedAfter) {
		super(insertedAfter);
		ISchedule s = RunEnvironment.getInstance().getCurrentSchedule();
		s.schedule(ScheduleParameters.createOneTime(s.getTickCount() + 1), this, "initFollow");
		s.schedule(ScheduleParameters.createOneTime(s.getTickCount() + 1), this, "initBlock");
		s.schedule(ScheduleParameters.createPoissonProbabilityRepeating(s.getTickCount() + ParamHelper.followChangeInterval,
				ParamHelper.followChangeInterval, ScheduleParameters.RANDOM_PRIORITY), this, "changeFollow");
		s.schedule(ScheduleParameters.createPoissonProbabilityRepeating(s.getTickCount() + ParamHelper.followBlockInterval,
				ParamHelper.followBlockInterval, ScheduleParameters.RANDOM_PRIORITY), this, "changeBlock");

	}

	@Override
	public void onReceive(Packet p, CustomEdge<Node> edge) {
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
				int next;
				if (store.getLog(e.getId()) == null) {
					next = 1;
				} else {
					if (store.getLog(e.getId()).getLatest() == null)
						next = 1;
					else
						next = store.getLog(e.getId()).getLatest().getIndex() + 1;
				}
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
				updateStore();
				edge.take(new FrontierMessage(this.store.getFrontier(this.store.getIDs()), this), this);
			}
			FrontierMessage fm = (FrontierMessage) p;

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

	public void follow(Node n) {
		InterestOperation op = new InterestOperation(Operation.FOLLOW, this.privateKey, n.getPublicKey());
		this.addEvent(op);
	}

	public void unfollow(Node n) {
		InterestOperation op = new InterestOperation(Operation.UNFOLLOW, this.privateKey, n.getPublicKey());
		this.addEvent(op);
	}

	public void block(Node n) {
		InterestOperation op = new InterestOperation(Operation.BLOCK, this.privateKey, n.getPublicKey());
		this.addEvent(op);
	}

	public void unblock(Node n) {
		InterestOperation op = new InterestOperation(Operation.UNBLOCK, this.privateKey, n.getPublicKey());
		this.addEvent(op);
	}

	public boolean isFollowed(Node n) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBlocked(Node n) {
		// TODO Auto-generated method stub
		return false;
	}

	public void initFollow() {
		int nFollow = ParamHelper.nFollowed;
		ArrayList<Node> toFollow = new ArrayList<Node>();
		do {
			for (Object o : ContextHelper.context.getRandomObjects(Node.class, nFollow - toFollow.size())) {
				if (o != this && !toFollow.contains(o)) {
					toFollow.add((Node) o);
				}
			}
		} while (toFollow.size() < nFollow);

		for (Node n : toFollow) {
			follow(n);
			ContextHelper.followGraph.addEdge(this, n);
		}
	}

	public void initBlock() {
		int nBlocked = ParamHelper.nBlocked;
		ArrayList<Node> toBlock = new ArrayList<Node>();
		do {
			for (Object o : ContextHelper.context.getRandomObjects(Node.class, nBlocked - toBlock.size())) {
				if (o != this && !toBlock.contains(o)) {
					toBlock.add((Node) o);
				}
			}
		} while (toBlock.size() < nBlocked);

		for (Node n : toBlock) {
			block(n);
		}
	}

	@Override
	protected void updateStore() {
		for (PublicKey f : this.store.getTransitivelyFollowedBy(this.getPublicKey(), 2)) {
			if (!this.store.getIDs().contains(f)) {
				this.store.addLog(new Log(f));
				followedFrom.put(f, RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
			}
		}
		for (PublicKey f : this.store.getTransitivelyBlockedBy(this.getPublicKey(), 2)) {
			if (this.store.getIDs().contains(f)) {
				this.store.removeLog(f);
				followedFrom.remove(f);
			}
		}
	}

	public void changeFollow() {
		Random r = new Random();
		if (r.nextFloat() < ParamHelper.followChangeProb) {
			Set<PublicKey> keys = this.store.getTransitivelyFollowedBy(this.getPublicKey(), 1);
			if (keys.size() > 0) {
				PublicKey sel = (PublicKey) keys.toArray()[r.nextInt(keys.size())];
				Node change;
				do {
					change = getRandomNode();
				} while (keys.contains(change.getPublicKey()));
				unfollow(NodeFinder.getNode(sel));
				ContextHelper.followGraph.removeEdge(ContextHelper.followGraph.getEdge(this, NodeFinder.getNode(sel)));
				follow(change);
				ContextHelper.followGraph.addEdge(this, change);
			}
		}
	}

	public void changeBlock() {
		Random r = new Random();
		if (r.nextFloat() < ParamHelper.blockChangeProb) {
			Set<PublicKey> keys = this.store.getTransitivelyBlockedBy(this.getPublicKey(), 1);
			if (keys.size() > 0) {
				PublicKey sel = (PublicKey) keys.toArray()[r.nextInt(keys.size())];
				Node change;
				do {
					change = getRandomNode();
				} while (keys.contains(change.getPublicKey()));
				unblock(NodeFinder.getNode(sel));
				block(change);
			}
		}
	}
	
	public double getFollowedFrom(PublicKey key) {
		Double value = this.followedFrom.get(key);
		return value==null?0d:value;
		
	}
}
