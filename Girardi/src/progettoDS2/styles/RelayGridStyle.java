package progettoDS2.styles;

import java.awt.Color;
import java.awt.Font;

import progettoDS2.actors.Relay;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class RelayGridStyle extends DefaultStyleOGL2D {
  @Override
  public String getLabel(Object object) {
    return object.toString();
  }
  @Override
  public Color getColor(Object agent) {
    if(((Relay)agent).locTick == 0) {
      return Color.blue;
    }
    return Color.black;
  }
  @Override
  public Font getLabelFont(Object o) {
    return new Font("Monospaced", Font.PLAIN, 12);
  }
}
