package scuttlebutt.styles;

import java.awt.Color;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import scuttlebutt.configuration.ScuttlebuttConfiguration;
import scuttlebutt.store.Store;

/**
 * Class to define style of nodes when represented on the display that shows the dimension of the
 * store, i.e. the number of events stored
 */
public class StoreStyle extends DefaultStyleOGL2D {

  /**
   * Define node color based on store dimension
   */
  @Override
  public Color getColor(Object object) {
    if (object instanceof Store) {
      Store store = (Store) object;
      int numberOfEvents = store.getNumberOfEvents();
      int loadScale = ScuttlebuttConfiguration.STORE_LOAD_COLOR_SCALE;

      if (numberOfEvents < loadScale * 5) {
        return Color.GREEN;
      } else if (numberOfEvents < loadScale * 6) {
        return Color.ORANGE;
      } else if (numberOfEvents < loadScale * 7) {
        return Color.RED;
      }
    }
    return Color.BLACK;
  }


  /**
   * Define node dimension based on store dimension
   */
  @Override
  public float getScale(Object object) {
    if (object instanceof Store) {
      Store store = (Store) object;
      return Math.max(0.5f, Math.min(5f,
          ((float) store.getNumberOfEvents()) / ScuttlebuttConfiguration.STORE_LOAD_COLOR_SCALE));
    }
    return 1f;
  }
}
