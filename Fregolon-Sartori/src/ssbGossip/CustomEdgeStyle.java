package ssbGossip;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;


public class CustomEdgeStyle implements EdgeStyleOGL2D {
	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		return 1;
	}

	//change the color of the edge based on the state
	@Override
	public Color getColor(RepastEdge<?> edge) {
		CustomEdge<?> e = (CustomEdge<?>) edge;
		Color c;
		switch (e.getState()) {
			case EMPTY:
				c = Color.GREEN;
				break;
			case ACTIVE:
				c = Color.YELLOW;
				break;
			case FULL:
				c = Color.RED;
				break;
			default:
				c = Color.GREEN;
				break;
		}
		return c;
	}
}
