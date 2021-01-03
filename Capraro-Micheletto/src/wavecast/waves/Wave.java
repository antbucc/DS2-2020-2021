package wavecast.waves;

/** Basic wave representation */
public class Wave implements Comparable<Wave> {

  /** Tick when the wave should be delivered */
  protected int deliveryTime;

  /** Relay that has sent/forwarded this wave */
  protected int forwarderID;


  public Wave() {}

  public Wave(int forwarderID) {
    this.forwarderID = forwarderID;
  }

  public Wave(int deliveryTime, int forwarderID) {
    this.deliveryTime = deliveryTime;
    this.forwarderID = forwarderID;
  }


  public int getForwarderID() {
    return forwarderID;
  }

  public void setForwarderID(int forwarderID) {
    this.forwarderID = forwarderID;
  }

  public int getDeliveryTime() {
    return deliveryTime;
  }

  public void setDeliveryTime(int deliveryTime) {
    this.deliveryTime = deliveryTime;
  }


  @Override
  public int compareTo(Wave that) {
    return Integer.compare(this.deliveryTime, that.deliveryTime);
  }
}
