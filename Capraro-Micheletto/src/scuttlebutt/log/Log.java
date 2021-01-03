package scuttlebutt.log;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import scuttlebutt.crypto.Crypto;
import scuttlebutt.event.Content;
import scuttlebutt.event.Event;
import scuttlebutt.exception.CryptoException;
import scuttlebutt.exception.SerializationException;
import scuttlebutt.serialization.Serializer;

/**
 * List of events stored in a store, relative to a particular id
 */
public class Log {

  private static final Logger LOGGER = Logger.getLogger(Log.class.getName());

  /** List of stored events */
  private List<Event> log;

  /** Index of the last stored event */
  private int lastEventStored;

  /** Id of the store relative to the log */
  private PublicKey publicKey;


  /**
   * Create a log from a public key
   */
  public Log(PublicKey publicKey) {
    this.log = new ArrayList<Event>();
    this.publicKey = publicKey;
    this.lastEventStored = -1;
  }


  /**
   * Copy a log from another log
   */
  public Log(Log log) {
    this.log = log.get();
    this.lastEventStored = log.getLastEventStored();
    this.publicKey = log.getId();
  }


  public int getLastEventStored() {
    return lastEventStored;
  }


  public void setLastEventStored(int lastEventStored) {
    this.lastEventStored = lastEventStored;
  }


  /**
   * Extend the log with a new event created locally (as owner) from content
   */
  public void append(Content content, PrivateKey privateKey, Crypto crypto)
      throws SerializationException {
    Integer previous = null;
    int size = this.log.size();
    if (size > 0) {
      previous = this.get().get(size - 1).hashCode();
    }

    Event newEvent = new Event(this.publicKey, previous, size, content);
    String signature = "";
    try {
      signature = crypto.encrypt(privateKey, Serializer.serialize(newEvent));
    } catch (CryptoException | SerializationException e) {
      LOGGER.log(Level.SEVERE, "Failed to generate signature for event", e);
    }
    newEvent.setSignature(signature);

    log.add(newEvent);
    this.lastEventStored = newEvent.getIndex();
  }


  /**
   * Extend the log with the subset of compatible events previously created remotely
   */
  public void update(List<Event> events) {
    for (Event event : events) {
      this.log.add(event);
      this.lastEventStored = event.getIndex();
    }
  }


  /**
   * Get the set of events with index included between start and end
   */
  public List<Event> get(int start, int end) {
    List<Event> result;
    try {
      result = log.subList(start, end + 1);
    } catch (IndexOutOfBoundsException e) {
      result = null;
    }

    return result;
  }


  public PublicKey getId() {
    return this.publicKey;
  }


  /**
   * Get log content (list of events)
   */
  public List<Event> get() {
    return this.log;
  }
}
