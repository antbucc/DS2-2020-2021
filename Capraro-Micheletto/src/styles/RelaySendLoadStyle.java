package styles;

import java.awt.Color;
import configuration.Configuration;
import relay.Relay;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class RelaySendLoadStyle extends DefaultStyleOGL2D {


  @Override
  public Color getColor(Object object) {
    if (object instanceof Relay) {
      Relay relay = (Relay) object;
      switch (relay.getSendLoad() / Configuration.LOAD_COLOR_SCALE) {
        case 0:
          return Color.black;
        case 1:
          return Color.yellow;
        case 2:
          return Color.orange;
        case 3:
        default:
          return Color.red;
      }
    }
    return Color.blue;
  }

}
