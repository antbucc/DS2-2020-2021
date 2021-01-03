package scuttlebutt.crypto;

import java.io.Serializable;

/**
 * Payload generated after the encryption process, to be sent on the network
 */
public class EncryptedPayload implements Serializable {

  private static final long serialVersionUID = -8301774993688495500L;

  /** AES shared secret encrypted with RSA */
  public byte[] secretKey;

  /** AES encryption of the Scuttlebutt payload */
  public byte[] payload;


  /**
   * Wrapper for unencrypted payload, when sent in clear
   * 
   * @param payload: the plain payload
   */
  public EncryptedPayload(byte[] payload) {
    this.payload = payload;
  }


  /**
   * Constructor for EncryptedPayload
   * 
   * @param secretKey: the encrypted shared secret
   * @param payload: the encrypted payload
   */
  public EncryptedPayload(byte[] secretKey, byte[] payload) {
    this.secretKey = secretKey;
    this.payload = payload;
  }
}
