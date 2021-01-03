package wavecast.styles;

import java.awt.Color;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.EdgeStyleOGL2D;
import wavecast.network.CustomEdge;

public class EdgeStyle implements EdgeStyleOGL2D {


  @Override
  public int getLineWidth(RepastEdge<?> edge) {
    CustomEdge<?> customEdge = (CustomEdge<?>) edge;
    if (customEdge.isShowInNetwork()) {
      return 3;
    }
    return 1;
  }

  @Override
  public Color getColor(RepastEdge<?> edge) {
    CustomEdge<?> customEdge = (CustomEdge<?>) edge;
    if (customEdge.isShowInNetwork()) {
      return Color.red;
    }
    return Color.black;
  }

}

