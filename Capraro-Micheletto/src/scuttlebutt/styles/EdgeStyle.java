package scuttlebutt.styles;

import java.awt.Color;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;

/**
 * Define style for edges in the displays
 */
public class EdgeStyle implements EdgeStyleOGL2D {

  /**
   * Define black colors for all the edges, as they have not a particular meaning
   */
  @Override
  public Color getColor(RepastEdge<?> edge) {
    return Color.black;
  }


  /**
   * Define line width equal to 1 for all the edges, as they have not a particular meaning
   */
  @Override
  public int getLineWidth(RepastEdge<?> edge) {
    return 1;
  }

}

