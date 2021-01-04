package interfaces;

import java.security.PublicKey;

import ssbGossip.CustomEdge;


public interface Node {

	void onReceive(Packet p, CustomEdge<Node> edge);

	PublicKey getPublicKey();

	void synchronizeStores();

	void fail();
	
	boolean isFailed();
	
	public boolean isInsertedAfter();
	
	public double getTickCreation();

}
