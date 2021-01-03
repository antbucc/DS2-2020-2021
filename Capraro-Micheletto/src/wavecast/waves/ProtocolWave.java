package wavecast.waves;

/** A wave used to exchange information as part of the protocol */
public class ProtocolWave extends Wave {

  /** ID of the relay that generated the original wave */
  private int sourceID;
  /** Reference of the wave */
  private int reference;
  /**
   * Value contained in the wave
   */
  private String value;

  public ProtocolWave(int sourceID, int reference, String value, int forwarderID) {
    super(forwarderID);
    this.sourceID = sourceID;
    this.reference = reference;
    this.value = value;
  }

  public ProtocolWave(int sourceID, int reference, String value, int deliveryTime,
      int forwarderID) {
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

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public String toString() {
    return String.format("Wave %s generated from source %d: value: %s, delivery: %d",
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
