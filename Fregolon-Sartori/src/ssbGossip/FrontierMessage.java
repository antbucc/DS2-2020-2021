package ssbGossip;

import interfaces.Node;
import interfaces.Packet;


public class FrontierMessage implements Packet {
	private Frontier frontier;
	private Node sender;

	public FrontierMessage(Frontier frontier, Node sender) {
		this.frontier = frontier;
		this.sender = sender;
	}

	public Frontier getFrontier() {
		return frontier;
	}

	public Node getSender() {
		return sender;
	}
	
	@Override
	public int getSize() {
		return (int) 0.25*frontier.frontier.size();
	}
}
