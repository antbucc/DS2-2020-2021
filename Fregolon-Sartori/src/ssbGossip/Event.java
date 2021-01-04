package ssbGossip;

import java.security.PrivateKey;
import java.security.PublicKey;

import interfaces.Packet;
import repast.simphony.engine.environment.RunEnvironment;


class EventContent { }


class InterestOperation extends EventContent {
	enum Operation {
		FOLLOW, UNFOLLOW,
		BLOCK, UNBLOCK
	}
	
	public Operation operation;
	PrivateKey privateKey;
	PublicKey targetID;
	
	public InterestOperation(Operation op, PrivateKey pk, PublicKey target) {
		this.operation = op;
		this.privateKey = pk;
		this.targetID = target;
	}
	
	public String toString() {
		return this.operation.toString() + ' ' + this.targetID.hashCode();
	}
}


public class Event implements Comparable<Event>, Packet {
	private PublicKey id;
	private Event previous;
	private int index;
	private EventContent content;
	private transient byte[] signature;
	private double tickCreation;
	private int size;
	
	public Event(PublicKey id, Event previous, int index, EventContent content, int size) {
		this.id = id;
		this.previous = previous;
		this.index = index;
		this.content = content;
		this.tickCreation = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		this.size = size;
	}
	
	@Override
	public int compareTo(Event e2) {
		return this.index - e2.index;
	}

	public PublicKey getId() {
		return id;
	}

	public Event getPrevious() {
		return previous;
	}

	public int getIndex() {
		return index;
	}

	public EventContent getContent() {
		return content;
	}

	public byte[] getSignature() {
		return signature;
	}
	
	public void sign(byte[] signature) {
		this.signature = signature;
	}

	public double getTickCreation() {
		return tickCreation;
	}
	
	public int getSize() {
		return size;
	}
}
