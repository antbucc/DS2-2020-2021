package styles;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class P2SNetStyle extends DefaultEdgeStyleOGL2D {

	public Color getColor(RepastEdge<?> edge) {
		return Color.MAGENTA;
	}
}