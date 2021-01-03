package scuttlebutt.payload;

import static scuttlebutt.payload.Payload.Type.HEARTBEAT;
import static scuttlebutt.payload.Payload.Type.PULL;
import static scuttlebutt.payload.Payload.Type.PUSH;
import static scuttlebutt.payload.Payload.Type.PUSHPULL;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;
import scuttlebutt.event.Event;
import scuttlebutt.store.Frontier;

/**
 * Content of the messages that will be encrypted and sent over the network during updates and
 * heartbeat broadcasting
 */
public class Payload implements Serializable {

  private static final long serialVersionUID = 4254647575747766798L;

  /** Frontier sent during updates */
  private Frontier frontier;

  /** News sent during updates */
  private List<Event> news;

  /** Id of the sender of the payload */
  private PublicKey senderId;

  /** Available types of the payload */
  public enum Type {
    PUSH, PUSHPULL, PULL, HEARTBEAT
  };

  /** Actual type of the payload */
  private Type type;


  /**
   * Constructor for PULL payload
   * 
   * @param senderId
   * @param frontier
   */
  public Payload(PublicKey senderId, Frontier frontier) {
    this.setSenderId(senderId);
    this.setFrontier(frontier);
    this.type = PULL;
  }


  /**
   * Constructor for PUSHPULL payload
   * 
   * @param senderId
   * @param frontier
   * @param news
   */
  public Payload(PublicKey senderId, Frontier frontier, List<Event> news) {
    this.setSenderId(senderId);
    this.setFrontier(frontier);
    this.setNews(news);
    this.type = PUSHPULL;
  }


  /**
   * Constructor for PUSH payload
   * 
   * @param senderId
   * @param news
   */
  public Payload(PublicKey senderId, List<Event> news) {
    this.setSenderId(senderId);
    this.setNews(news);
    this.type = PUSH;
  }


  /**
   * Constructor for HEARTBEAT payload
   * 
   * @param senderId
   */
  public Payload(PublicKey senderId) {
    this.setSenderId(senderId);
    this.type = HEARTBEAT;
  }


  public Frontier getFrontier() {
    return frontier;
  }


  public void setFrontier(Frontier frontier) {
    this.frontier = frontier;
  }


  public List<Event> getNews() {
    return news;
  }


  public void setNews(List<Event> news) {
    this.news = news;
  }


  public Type getType() {
    return this.type;
  }


  public void setType(Type type) {
    this.type = type;
  }


  public PublicKey getSenderId() {
    return senderId;
  }


  public void setSenderId(PublicKey senderId) {
    this.senderId = senderId;
  }


  public int getNumberOfNews() {
    return this.news.size();
  }
}
