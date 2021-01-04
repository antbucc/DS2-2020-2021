package progettoDS2.actors;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import net.i2p.crypto.eddsa.KeyPairGenerator;
import progettoDS2.Medium;
import progettoDS2.messages.Message;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;


@AgentAnnot(displayName = "Relay")
abstract public class Relay {
  /**
   * Both the storage and the frontier, stores messages
   * and saves the newest one to get the reference
   */
  Map<List<Byte>, Log> frends;
  /**
   * PrivateKey and PublicKey are saved here
   */
  KeyPair identity;
  /**
   * The medium abstraction, used to send messages
   */
  Medium medium;
  /**
   * Local time of the relay, counts the number of times the
   * application tick was called
   */
  Integer localClock;
  /**
   * Used to scatter the ticks, it can either be:
   * - 0: the application tick was just called
   * - non 0: the application tick is not going to be called
   */
  public Integer locTick;
  /**
   * Used to display the system
   */
  Integer posX;
  /**
   * Used to display the system
   */
  Integer posY;
  /**
   * Used to buffer the messages till the next tick
   */
  ArrayList<Message> bufferMsg;
  /**
   * Method used to implement an application level protocol, 
   * all messages received are sent to this method
   */
  abstract void appReceiveMessage(Message msg);
  /**
   * Method used to implement an application level protocol, 
   * this is called once every application tick
   */
  abstract void applicationTick();
  /**
   * Integer reference used in messages
   */
  Integer ref;
  /**
   * Creates a relay initializing and generating the identity
   * @param m The medium that can be used to send messages
   */
  public Relay(Medium m) {
    medium = m;
    identity = generateIdentity();
    frends = new HashMap<>();
    frends.put(this.getIdentity(), new Log(this.getIdentity()));
    locTick = RandomHelper.nextIntFromTo(0, 10);
    bufferMsg = new ArrayList<>();
    ref = 0;
    localClock = 0;
  }
  /**
   * Fetches the log for this
   * @return The log for this
   */
  public Log getLog() {
    return frends.get(this.getIdentity());
  }
  public Integer getPrev() {
    Log l = getLog();
    if(l.Newest != null) {
      return l.Newest.getPrevious();
    }
    return null;
  }
  /**
   * Fetches message reference and updates it
   * @return The reference before update
   */
  public Integer getNextRef() {
    ref = getNextRef(ref);
    return ref;
  }
  /**
   * Inserts a message in the local log
   * @param m The message that has to be insert
   */
  public void localMessageInsert(Message m) {
    Log l = getLog();
    if(m.getSignature() == null) {
      m.generateSignature(this.identity.getPrivate());
    }
    //System.out.println("Calling add message");
    if(!l.addMessage(m)) {
    }
  }
  /**
   * Used to display the system
   */
  public void setPosition(int x, int y) {
    posX = x;
    posY = y;
  }
  /**
   * Used to display the system
   */
  public Integer getX() {
    return posX;
  }
  /**
   * Used to display the system
   */
  public Integer getY() {
    return posY;
  }
  /**
   * Generates a keypair that identifies the Relay, override this method to
   * load it from local storage
   */
  KeyPair generateIdentity() {
    KeyPairGenerator kpg = new KeyPairGenerator();
    return kpg.generateKeyPair();
  }
  /**
   * Used to mask the actual tick, this calls the class method once each
   * X ticks, depending on the parameter, to lower the load on the CPU
   */
  @ScheduledMethod(start = 1, interval = 1)
  public void tick() {
    locTick += 1;
    locTick %= 10;
    if(locTick == 0) { //scatter out the ticks, to have better performances
      deliverMessagesInBuffer();
      applicationTick();
      localClock += 1;
    }
  }

  /**
   * Get the Log that has sent this message, if the log does
   * not exist, create it and return it 
   * @param msg: The message containing the identity
   * @return Either a new log or the existing one
   */
  Log getTarget(Message msg) {
    Log target = null;
    if (!frends.containsKey(msg.getFrom())) { 
      if(msg.getPrevious() == null) {
        target = new Log(msg.getFrom());
        frends.put(msg.getFrom(), target);
      }
    } else {
      target = frends.get(msg.getFrom());
    }
    return target;
  }
  /**
   * Delivers all messages to the app that are due to this tick
   */
  void deliverMessagesInBuffer() {
    for(Message m : bufferMsg) {
      appReceiveMessage(m);
    }
    bufferMsg.clear();
  }
  /**
   * Called by the medium when a message is received
   */
  public void onSense(Object message) {
    bufferMsg.add((Message)message);
    //appReceiveMessage((Message)message);    
  }
  /**
   * Generates the next message reference starting from an existing one
   * @param ref A reference
   * @return The next reference
   */
  Integer getNextRef(Integer ref) {
    return ref + 1;
  }
  /**
   * Forward a message in the medium
   * @param msg The message
   */
  void forward(Message msg) {
    if(msg.getSignature() == null) {
      msg.generateSignature(this.identity.getPrivate());
    }
    medium.send(msg, this);
  }
  /**
   * Used to display the system
   */
  @Override
  public String toString() {
    return identity.toString();
  }
  /**
   * Fetches the byte[] repr of the public key
   * @return The byte[] repr of the public key
   */
  public List<Byte> getIdentity() {
    return Arrays.asList(ArrayUtils.toObject(identity.getPublic().getEncoded()));
  }
  /**
   * Forgets a peer's log
   * @param identity The identity to forget
   */
  public void eraseLog(List<Byte> identity) {
    frends.remove(identity); 
  }
}
