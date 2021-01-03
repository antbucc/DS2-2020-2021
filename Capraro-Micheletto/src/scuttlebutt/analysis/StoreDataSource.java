package scuttlebutt.analysis;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import repast.simphony.data2.AggregateDataSource;
import scuttlebutt.store.Frontier;
import scuttlebutt.store.Store;

/**
 * This class provides an aggregate data source to be plotted by Repast, by computing the number of
 * stores that are waiting for updates in order to gather all the needed events
 */
public class StoreDataSource implements AggregateDataSource {
  /** Set of stores that have not received yet all the needed news */
  private Set<Store> storesNotConverged;

  /** Frontier with latest event for each id */
  private Frontier frontier;


  public StoreDataSource() {
    super();
    storesNotConverged = new HashSet<Store>();
    frontier = new Frontier();
  }

  @Override
  public String getId() {
    return "store";
  }

  @Override
  public Class<?> getDataType() {
    return Integer.class;
  }

  @Override
  public Class<?> getSourceType() {
    return Store.class;
  }


  /**
   * Return the number of stores that are waiting to receive some events through updates
   */
  @Override
  public Object get(Iterable<?> objs, int size) {

    storesNotConverged = StreamSupport.stream(objs.spliterator(), false)
        .filter(obj -> obj instanceof Store).map(obj -> (Store) obj).peek(store -> {
          frontier.put(store.getPublicKey(), store.getFrontier().get(store.getPublicKey()));
        }).collect(Collectors.toSet());


    if (storesNotConverged.size() == 0) {
      return 0;
    }

    StreamSupport.stream(objs.spliterator(), false).filter(obj -> obj instanceof Store)
        .map(obj -> (Store) obj).forEach(store -> {
          Frontier littleFrontier = store.getFrontier();
          boolean converged = true;
          for (PublicKey key : littleFrontier.keySet()) {
            if (littleFrontier.get(key) < frontier.get(key)) {
              converged = false;
              break;
            }
          }
          if (converged) {
            storesNotConverged.remove(store);
          }
        });

    return storesNotConverged.size();
  }


  @Override
  public void reset() {}

}
