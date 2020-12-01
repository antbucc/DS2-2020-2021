package progettoDS2.styles;

import java.awt.Font;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class RelayGridStyle extends DefaultStyleOGL2D {
  @Override
  public String getLabel(Object object) {
    return object.toString();
  }
  @Override
  public Font getLabelFont(Object o) {
    return new Font("Monospaced", Font.PLAIN, 12);
  }
}
