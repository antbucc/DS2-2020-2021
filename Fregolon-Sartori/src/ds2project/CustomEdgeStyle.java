package ds2project;

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
		CustomEdge<Object> e = (CustomEdge<Object>) edge;
		Color c;
		switch (e.getState()) {
			case 0:
				c = Color.GREEN;
				break;
			case 1:
				c = Color.YELLOW;
				break;
			case 2:
				c = Color.RED;
				break;
			default:
				c = Color.GREEN;
				break;
		}
		return c;
	}
}
