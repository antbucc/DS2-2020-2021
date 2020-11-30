package progettoDS2.styles;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;

public class ColoredEdge extends RepastEdge<Object>{
  Color color;
  public ColoredEdge(Object start, Object end, boolean directed, Color color) {
    super(start,end,directed);
    this.color = color;
  }
  public Color getColor() {
    return this.color;
  }
}