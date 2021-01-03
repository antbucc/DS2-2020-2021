package scuttlebutt.styles;

import java.awt.Color;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import scuttlebutt.store.Store;

/**
 * Define style of nodes in the display that shows following relationships wrt one specified node
 */
public class StoreReplicationStyle extends DefaultStyleOGL2D {

  /** Available node styles for the different relationships */
  public static enum style {
    NONE, MAIN, DIRECTED, TRANSITIVE, BLOCKED
  };


  /**
   * Define color based on the relationship with the specified node
   */
  @Override
  public Color getColor(Object object) {
    if (object instanceof Store) {
      Store store = (Store) object;
      switch (store.getStyle()) {
        case NONE:
          return Color.BLACK;
        case MAIN:
          return Color.BLUE;
        case DIRECTED:
          return Color.ORANGE;
        case TRANSITIVE:
          return Color.YELLOW;
        case BLOCKED:
          return Color.RED;
        default:
          return Color.BLACK;
      }
    }
    return Color.BLACK;
  }
}
