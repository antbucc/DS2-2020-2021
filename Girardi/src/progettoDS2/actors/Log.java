package progettoDS2.actors;

import progettoDS2.messages.Message;

import java.util.ArrayList;
import java.util.List;

public class Log {
  public List<Byte> Identity;
  ArrayList<Message> RecvFrom;
  Message Newest;
  public Log(List<Byte> identity) {
    Identity = identity;
    RecvFrom = new ArrayList<>();
    Newest = null;
  }
  /**
   * Add a message to the log, this operation is succesfull only if
   * - The message is Valid (refer to Message::Validate)
   * - The message is newer than the newest recorded
   * - The message has Newest as Previous
   * @param m Message to be added
   * @return true if inserted, else false
   */
  public boolean addMessage(Message m) {
    if( m.Validate()) {
      if(Newest != null) {
        if(!(m.compare(m, Newest) > 0 &&
          m.getPrevious() == Newest.getToBePrevious())
          ) {
          return false;
        }
      }
      RecvFrom.add(m);
      Newest = m;
      return true;
    }
    /*System.out.print("Validate: ");
    System.out.println(m.Validate());
    System.out.print("compare: ");
    System.out.println(m.compare(m, Newest) > 0 );
    System.out.print("getPrevious: ");
    System.out.println( m.getPrevious() == Newest.getToBePrevious());*/
    return false;
  }
  /**
   * Get all the messages from: from to: to
   * @param from lower bound
   * @param to upper bound
   */
  public ArrayList<Message> getFromTo(int from, int to){
    ArrayList<Message> toReturn = new ArrayList<Message>();
    for(Message m : this.RecvFrom) {
      Integer ref = m.getRef();
      if(from <= ref && to >= ref) {
        toReturn.add(m);
      }
    }
    return toReturn;
  }
}
