package wavecast.styles;

import java.awt.Color;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import wavecast.relay.Relay;

/** Show the relay current behaviour */
public class RelayStyle extends DefaultStyleOGL2D {

  public static enum style {
    SEND, FORWARD, IDLE
  };

  @Override
  public Color getColor(Object object) {
    if (object instanceof Relay) {
      Relay relay = (Relay) object;
      switch (relay.getStyle()) {
        case SEND:
          return Color.RED;
        case FORWARD:
          return Color.ORANGE;
        case IDLE:
        default:
          return Color.GREEN;

      }
    }
    return Color.BLACK;
  }
}
