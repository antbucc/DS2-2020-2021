package scuttlebutt.store.protocol;

import java.security.PublicKey;
import scuttlebutt.store.Frontier;
import scuttlebutt.store.Store;


/**
 * Interface for OpenGossip and TransitiveInterest protocols
 */
public interface Protocol {

  public abstract void processHeartbeat(Store store, PublicKey id);

  public abstract Frontier getFrontier(Store store);

}
