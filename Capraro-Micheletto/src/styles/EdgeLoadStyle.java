package styles;

import java.awt.Color;
import configuration.Configuration;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;

public class EdgeLoadStyle implements EdgeStyleOGL2D {
  @Override
  public int getLineWidth(RepastEdge<?> edge) {
    return 1;
  }

  @Override
  public Color getColor(RepastEdge<?> edge) {
    double weight = edge.getWeight();

    switch ((int) weight / Configuration.LOAD_COLOR_SCALE) {
      case 0:
        return Color.yellow;
      case 1:
        return Color.orange;
      case 2:
        return Color.red;
      default:
        return Color.black;

    }
  }

}

