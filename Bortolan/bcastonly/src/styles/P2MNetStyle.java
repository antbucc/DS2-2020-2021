package styles;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class P2MNetStyle extends DefaultEdgeStyleOGL2D {

	public Color getColor(RepastEdge<?> edge) {
		return Color.ORANGE;
	}
}