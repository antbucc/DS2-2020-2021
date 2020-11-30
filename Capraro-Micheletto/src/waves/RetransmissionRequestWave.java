package waves;

import java.util.Map;

/** Wave used to request a retransmission of lost messages */
public class RetransmissionRequestWave extends Wave {

  /** Tick when the request was sent */
  private int sendTick;

  /** Frontier of the request issuer */
  private Map<Integer, Integer> frontier;

  public RetransmissionRequestWave(int forwarderID, int sendTick, Map<Integer, Integer> frontier,
      int deliveryTime) {
    super(deliveryTime, forwarderID);
    this.sendTick = sendTick;
    this.frontier = frontier;
  }

  public int getSendTick() {
    return sendTick;
  }

  public void setSendTick(int sendTick) {
    this.sendTick = sendTick;
  }

  public Map<Integer, Integer> getFrontier() {
    return frontier;
  }

  public void setFrontier(Map<Integer, Integer> frontier) {
    this.frontier = frontier;
  }
}
