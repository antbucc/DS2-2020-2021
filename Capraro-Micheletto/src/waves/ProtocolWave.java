package waves;

/** A wave used to exchange information as part of the protocol */
public class ProtocolWave extends Wave {

  /** ID of the relay that generated the original wave */
  private int sourceID;
  /** Reference of the wave */
  private int reference;
  /**
   * Value contained in the wave (currently we use the tick when the wave was generated as value)
   */
  private int value;

  public ProtocolWave(int sourceID, int reference, int value, int forwarderID) {
    super(forwarderID);
    this.sourceID = sourceID;
    this.reference = reference;
    this.value = value;
  }

  public ProtocolWave(int sourceID, int reference, int value, int deliveryTime, int forwarderID) {
    super(deliveryTime, forwarderID);
    this.sourceID = sourceID;
    this.reference = reference;
    this.value = value;

  }

  public ProtocolWave(ProtocolWave wave) {
    this(wave.sourceID, wave.reference, wave.value, wave.deliveryTime, wave.forwarderID);
  }

  public int getSourceID() {
    return sourceID;
  }

  public void setSourceID(int sourceID) {
    this.sourceID = sourceID;
  }

  public int getReference() {
    return reference;
  }

  public void setReference(int reference) {
    this.reference = reference;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }


  @Override
  public String toString() {
    return String.format("Wave %s generated from source %d: value: %d, delivery: %d",
        this.reference, this.sourceID, this.value, this.deliveryTime);
  }



  @Override
  public int compareTo(Wave that) {
    int sameDeliveryTime = Integer.compare(this.deliveryTime, that.deliveryTime);

    if (that instanceof ProtocolWave) {
      ProtocolWave protocolWave = (ProtocolWave) that;
      if (sameDeliveryTime == 0 && this.sourceID == protocolWave.getSourceID()) {
        return Integer.compare(this.getReference(), protocolWave.getReference());
      }
    }

    return sameDeliveryTime;
  }


}
