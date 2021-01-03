package scuttlebutt.store.protocol;

import java.security.PublicKey;
import scuttlebutt.log.Log;
import scuttlebutt.store.Frontier;
import scuttlebutt.store.Store;

/**
 * Open Gossip specific method implementations
 */
public class OpenGossipProtocol implements Protocol {

  /**
   * On heartbeat reception, always add log in the store (always follow)
   */
  @Override
  public void processHeartbeat(Store store, PublicKey id) {
    store.add(new Log(id));
  }


  /**
   * Compute frontier based on stored logs
   */
  @Override
  public Frontier getFrontier(Store store) {
    Frontier frontier = new Frontier();
    for (Log log : store.getLogs().values()) {
      frontier.put(log.getId(), log.getLastEventStored());
    }
    store.setDirectFollowee(frontier.keySet());
    return frontier;
  }

}
