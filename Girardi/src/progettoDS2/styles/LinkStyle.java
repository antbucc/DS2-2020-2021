package progettoDS2.styles;

import java.awt.Color;

import progettoDS2.styles.ColoredEdge;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class LinkStyle extends DefaultEdgeStyleOGL2D {
  @Override
  public Color getColor(RepastEdge<?> edge) {
    return ((ColoredEdge)edge).getColor();
  }
}