package ssbGossip;

import java.awt.Color;

import interfaces.Node;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class NodeStyle extends DefaultStyleOGL2D{
	@Override
	public Color getColor(Object agent) {
		Node n = (Node) agent;
		if(n.isFailed()) {
			return Color.RED;
		} else if (n.isInsertedAfter()){
			return Color.GREEN;
		} else {
			return Color.BLUE;
		}
	}

}
