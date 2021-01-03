package wavecast.waves;

/**
 * Wrapper used to distinguish between waves that are propagated as part of the retransmission
 * protocol
 */
public class RetransmissionProtocolWave extends ProtocolWave {

  public RetransmissionProtocolWave(int sourceID, int reference, String value, int deliveryTime,
      int forwarderID) {
    super(sourceID, reference, value, deliveryTime, forwarderID);
  }

}
