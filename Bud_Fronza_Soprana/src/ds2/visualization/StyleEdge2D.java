package ds2.visualization;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;

/* 
 * Style descriptor for edges;
 * To get weight and colors for the edge elements,
 * this class is calling methods from DisplayManager when needed
 */
public class StyleEdge2D implements EdgeStyleOGL2D {

	@Override
	public Color getColor(RepastEdge<?> edge) {
		return DisplayManager.getInstance().getNextColor("Edge");
	}

	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		return DisplayManager.getInstance().getEdgeWeight();
	}
}
