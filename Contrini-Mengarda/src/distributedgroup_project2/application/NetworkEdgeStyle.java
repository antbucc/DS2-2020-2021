package distributedgroup_project2.application;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;

public class NetworkEdgeStyle implements EdgeStyleOGL2D {

	/**
	 * Method to retrieve the width of the line of the edge.
	 * 
	 * @param edge: the input edge;
	 * @return: the width of the line of the edge.
	 */
	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		return 1;
	}

	/**
	 * Method to retrieve the color of the line of the edge depending on the edge type.
	 * 
	 * @param edge: the input edge (NetworkEdge);
	 * @return: the color of the line of the edge.
	 */
	@Override
	public Color getColor(RepastEdge<?> edge) {
		Color color = null;
		switch (((NetworkEdge) edge).getType()) {
			case FOLLOW:
				color = new Color(50, 50, 50);
				break;
			case BLOCK:
				color = new Color(255, 0, 0);
				break;
		}
		return color;
	}
	
}
