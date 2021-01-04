package ssbGossip;

import interfaces.Node;
import interfaces.Packet;


//class used to know who put the packet on the link (in order to deliver it to the other end of the link)
public class RelayedPacket {
	Packet packet;
	Node node;
	
	public RelayedPacket(Packet p, Node n) {
		this.packet = p;
		this.node = n;
	}
	
	public Packet getPacket() {
		return packet;
	}
	
	public Node getNode() {
		return node;
	}
}
