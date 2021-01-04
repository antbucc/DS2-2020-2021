/**
 * 
 */
package progettoDS2.actors;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import progettoDS2.Medium;
import progettoDS2.messages.*;
import progettoDS2.utils.RngHelper;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

/**
 * Concrete implementation of a relay, this both
 * support an automatic execution and a non automatic execution
 */
public class ConcreteRelay extends Relay {

  /**
   * Set of relays this is intrested in
   */
  ArrayList<List<Byte>> intrestList;
  /**
   * Set of relays this trusts
   */
  ArrayList<List<Byte>> trustList;
  /**
   * Set of relays this has blocked
   */
  ArrayList<List<Byte>> blockList;
  /**
   * Used to determine if a automatic test is running
   */
  boolean autoTest;
  
  Parameters p;
  double post_probability;
  double trust_probability;
  /**
   * Constructor, builds the concrete relay
   * @param m The medium that the ConcreteRelay will use to communicate
   */
  public ConcreteRelay(Medium m) {
    super(m);
    p = RunEnvironment.getInstance().getParameters();
    intrestList = new ArrayList<>();
    intrestList.add(this.getIdentity());
    trustList = new ArrayList<>();
    blockList = new ArrayList<>();
    autoTest = false;
    loadParameters();
  }
  public void loadParameters() {
    post_probability = p.getDouble("post_probability");
    trust_probability = p.getDouble("trust_probability");
  }

  /**
   * This method does:
   * - Check if the message is valid and intresting
   * - Creates the log if not present
   * - Disambiguate between messages
   * - Forward the message if it was intresting and valid
   */
  @Override
  void appReceiveMessage(Message msg) {
    if(!hasToReceive(msg)) {
      //take care of removing the message from local log
      this.eraseLog(msg.getFrom());
      return;
    }
    /*
     * If got here, register the message in the log because the message:
     * - Is of intrest
     * - Is valid
     */
    Log log = getTarget(msg);
    if(!log.addMessage(msg)) {
      /*
       * The message was either not valid or out of order
       */
      return; 
    }
    if(msg instanceof PostMessage) {
      rngTrust(msg);      
    }else if(msg instanceof BlockMessage) {
      if(trusted(msg.getFrom())) {
        onBlockMessage((BlockMessage)msg);
      }else {
        rngTrust(msg);
      }
    }else if(msg instanceof FollowMessage) {
      rngTrust(msg);
    }else if(msg instanceof UnblockMessage) {
      if(trusted(msg.getFrom())) {
        onUnblockMessage((UnblockMessage)msg);
      }else {
        rngTrust(msg);
      }
    }else if(msg instanceof UnfollowMessage) {
      rngTrust(msg);
    }else {
      //System.out.println("Discarded message");
    }
    forward(msg);
  }
  /**
   * Adds a host to trust list with probability trust_probability.
   * m might spark trust in sender, if so add it to trust list.
   * There is 0% probability to spark interest in an automatic test
   * @param m The message that might spark intrest
   */
  void rngTrust(Message m) {
    if(autoTest) {return;}
    if(RngHelper.shootWithProb(trust_probability)){
      if(!trustList.contains(m.getFrom())) {
        trustList.add(m.getFrom());
      }
    }
  }
  /**
   * Checks if from is someone this is intrested in
   * @param from The identity of the user to be chekced
   * @return true if intresting, false else
   */
  boolean isOfInterest(List<Byte> from) {
    return intrestList.contains(from);
  }
  /**
   * Checks if id is trusted by this
   * @param id The identity of the user to be checked
   * @return true if trusted, false else
   */
  boolean trusted(List<Byte> id) {
    return this.trustList.contains(id);
  }
  /**
   * Called when "blockMessage" reception
   * @param m The block message
   */
  void onBlockMessage(BlockMessage m)  {
    List<Byte> tgt = m.getTarget();
    if(!blockList.contains(tgt)){
      blockList.add(m.getTarget());
      BlockMessage blk = new BlockMessage(
        getIdentity(), 
        getNextRef(),
        ArrayUtils.toPrimitive((Byte[])tgt.toArray()), 
        getPrev()
      );
      localMessageInsert(blk);        
      forward(blk);      
    }
  }
  /**
   * Called on "unblockMessage" reception
   * @param m The unblock message
   */
  void onUnblockMessage(UnblockMessage m) {
    if(blockList.contains(m.getTarget())){
      byte[] tgt = ArrayUtils.toPrimitive((Byte[])m.getTarget().toArray());
      blockList.remove(m.getTarget());
      UnblockMessage ublk = new UnblockMessage(this.getIdentity(), this.getNextRef(), tgt, this.getPrev());
      try {
        localMessageInsert(ublk);
      } catch (Exception e) {
        //System.out.println("Validation failed on send unblock");
      }
      forward(ublk);
    }
  }
  /**
   * Checks wether this has blocked from
   * @param from The identity to be checked
   * @return true if blocked, false else
   */
  boolean isBlocked(List<Byte> from) {
    return blockList.contains(from);
  }
  /**
   * Checks if a message has to be received or removed
   * @param msg The message to be checked
   * @return true if it has to be received, false else
   */
  boolean hasToReceive(Message msg) {
    if(isBlocked(msg.getFrom()) ||
       !isOfInterest(msg.getFrom())) {
      return false;
    }
    return true;
  }

  /**
   * Called once every tick for this
   */
  @Override
  void applicationTick() {
    if(!autoTest) {
      if(RngHelper.shootWithProb(post_probability)) {
        PostMessage pst = new PostMessage(getIdentity(), getNextRef(), "PostMessage random content", getPrev());
        //try {
          localMessageInsert(pst);
        //}catch(Exception e) {
        //  System.out.println("Validation failed on send tick");
        //}
        forward(pst);
      }
      if(RngHelper.shootWithProb(0.1)) {
        
      }
    }
  }
  /**
   * Used to plan and automatically test the ConcreteRelay
   * @param pathInt Path to a intrest list
   * @param pathBlock Path to a block list
   * @throws Exception On any error, throw
   */
  @SuppressWarnings("unchecked")
  public void buildFromSpec(String pathInt, String pathBlock) throws Exception {
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(pathInt));
    intrestList = (ArrayList<List<Byte>>)in.readObject();
    in.close();
    ObjectInputStream in2 = new ObjectInputStream(new FileInputStream(pathBlock));
    blockList = (ArrayList<List<Byte>>)in2.readObject();
    in2.close();
    autoTest = true;
  }

}
