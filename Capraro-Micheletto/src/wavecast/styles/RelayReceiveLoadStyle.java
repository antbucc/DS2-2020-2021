package wavecast.styles;

import java.awt.Color;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import wavecast.configuration.WavecastConfiguration;
import wavecast.relay.Relay;

public class RelayReceiveLoadStyle extends DefaultStyleOGL2D {


  @Override
  public Color getColor(Object object) {
    if (object instanceof Relay) {
      Relay relay = (Relay) object;
      switch (relay.getReceiveLoad() / WavecastConfiguration.LOAD_COLOR_SCALE) {
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
