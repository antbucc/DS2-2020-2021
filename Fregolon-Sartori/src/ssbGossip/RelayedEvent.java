package ssbGossip;

import interfaces.Node;


public class RelayedEvent {
	Event event;
	Node node;
	
	public RelayedEvent (Event e, Node n) {
		this.event = e;
		this.node = n;
	}
	
	public Event getEvent() {
		return event;
	}
	
	public Node getNode() {
		return node;
	}
}
