package progettoDS2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.engine.schedule.ScheduledMethod;

abstract class Message implements Comparator<Message> {
  public String From;
  public Integer Ref;
  
  public Message(String from, Integer ref) {
    From = from;
    Ref = ref;
  }
  
  @Override
  public int compare(Message a1, Message a2) {
    return a1.Ref.compareTo(a2.Ref); 
  }  
}

class StdMessage extends Message {
  public String Val;

  public StdMessage(String from, Integer ref, String val) {
    super(from, ref);
    this.Val = val;
  }
}

class ArqMessage extends Message {
  public ArqMessage(String from, Integer ref) {
    super(from, ref);
  }
}

class Fren {
  public String Identity;
  ArrayList<Message> RecvFrom;
  Integer Newest;
  public Fren(String identity, Integer ref) {
    Identity = identity;
    RecvFrom = new ArrayList<>();
    Newest = -1;
  }
  
  public void addMessage(Message m) {
    assert(!(m instanceof ArqMessage));
    RecvFrom.add(m);
    if(m.Ref > Newest) {
      Newest = m.Ref;
    }
  }
  public Message getReceived(Integer Ref) {
    //FIXME: bad and slow
    for(Message m : RecvFrom) {
      if(m.Ref == Ref) {
        return m;
      }
    }
    return null;
  }
}

@AgentAnnot(displayName = "Relay")
public class Relay {
  Map<String, Fren> frends;
  String identity;
  Medium medium;
  PriorityQueue<Message> bag;
  Integer local_clock;
  Integer posX;
  Integer posY;

  public Relay(Medium m) {
    this.medium = m;
    this.identity = getIdentity();
    this.frends = new HashMap<>();
    this.bag = new PriorityQueue<>();
    
    local_clock = 0;
  }
  
  public void setPosition(int x, int y) {
    posX = x;
    posY = y;
  }
  
  public Integer getX() {
    return posX;
  }
  
  public Integer getY() {
    return posY;
  }
  
  String getIdentity() {
    int leftLimit = 'a'; // letter 'a'
    int rightLimit = 'z'; // letter 'z'
    int targetStringLength = 5;
    Random random = new Random();

    return random.ints(leftLimit, rightLimit + 1).limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
  }

  public boolean isKnown(String identity) {
    return frends.containsKey(identity);
  }
  
  public void stdSend(Message msg) { //TODO: refactor this shit
    msg.Ref = (int)local_clock;
    local_clock ++;
    this.medium.send(msg, this);
  }
  
  @ScheduledMethod(start = 1, interval = 5)
  public void tick() {    
    applicationTick();
    Collection<Fren> frens = this.frends.values();
    for(Fren f : frens) {
      this.medium.send(new ArqMessage(f.Identity, getNextRef(f.Newest)), this);
    }
  }
  public void testMessage() {
    stdSend(new StdMessage(this.identity, 0, "test message"));
  }

  Fren getTarget(Message msg) {
    Fren target;
    if (!isKnown(msg.From)) { //autojoin for now
      target = new Fren(msg.From, msg.Ref);
      frends.put(msg.From, target);
    } else {
      target = frends.get(msg.From);
    }
    return target;
  }
  
  void applicationReceiveMessage(StdMessage msg) {
    /*
     * The implementation of this method is delegated to 
     * subclasses 
     */
  }
  void applicationTick() {
 
  }
  
  void onStdMessageSense(StdMessage msg) {
    Fren f = getTarget(msg);
    if(msg.Ref == getNextRef(f.Newest)) {
      medium.send(msg, this); //forward
      f.addMessage(msg);
      applicationReceiveMessage(msg);
    }
  }
  
  void onArqMessageSense(ArqMessage msg) {
    Fren f = getTarget(msg);
    Message m = f.getReceived(msg.Ref);
    if(m != null) {
      this.medium.send(m, this);
    }
  }
  
  public void onSense(Object message) {
    if(message instanceof ArqMessage) {
      onArqMessageSense((ArqMessage)message);
    }else if(message instanceof StdMessage) {
      onStdMessageSense((StdMessage)message);
    }else {
      assert(false);
    }
  }

  Integer getNextRef(Integer ref) {
    return ref + 1;
  }
  
  @Override
  public String toString() {
    return identity;
  }
}
