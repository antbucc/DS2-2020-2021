package progettoDS2.styles;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.RepastEdge;

public class ColoredEdge extends RepastEdge<Object>{
  Color color;
  static Map<Pair<Object,Object>, Color> memory;
  Object start;
  Object end;
  public ColoredEdge(Object start, Object end, Color c) {
    super(start,end,false);
    this.start = start;
    this.end = end;
    this.color = c;
  }
  
  Color getColorOfForCouple() {
    double hHigh = 200;
    double hLow = 80;
    float s = (float)RandomHelper.nextDoubleFromTo(30, 100);
    float v = (float)RandomHelper.nextDoubleFromTo(90, 100);
    float h = (float)RandomHelper.nextDoubleFromTo(hLow, hHigh);
    Color c = Color.getHSBColor(h, s, v);
    return c;
  }
  
  private Color generateColor() {
    Pair<Object, Object> key = new Pair<>(start, end);
    if(memory == null) {
      memory = new HashMap<>();
    }
    if(!memory.containsKey(key)) {
      memory.put(key, getColorOfForCouple());
    }
    return memory.get(key);
  }
  
  public ColoredEdge(Object start, Object end) {
    super(start, end, false);
    this.start = start;
    this.end = end;
    this.color = generateColor();
  }
  
  public Color getColor() {
    return this.color;
  }
}