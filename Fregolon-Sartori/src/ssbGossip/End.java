package ssbGossip;

import interfaces.Packet;


public class End implements Packet {
	@Override
	public int getSize() {
		return 50;
	}
}
