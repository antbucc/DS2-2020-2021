package scuttlebutt.crypto;


import static org.junit.Assert.assertEquals;
import java.security.KeyPair;
import org.junit.Test;
import scuttlebutt.exception.CryptoException;
import scuttlebutt.exception.SerializationException;
import scuttlebutt.payload.Payload;
import scuttlebutt.serialization.Serializer;
import scuttlebutt.store.Frontier;

/**
 * JUnit test to perform testing on crypto functionalities
 */
public class CryptoTest {

  /**
   * Test whether a public key is properly able to decrypt a text encrypted with the correspondent
   * private key
   * 
   * @throws CryptoException
   */
  @Test
  public void encryptPrivateDecryptPublic() throws CryptoException {
    Crypto crypto = new Crypto();
    KeyPair keypair = crypto.newKeyPair();

    String text = "to-be-encrypted";
    String encrypted = crypto.encrypt(keypair.getPrivate(), text);
    String decrypted = crypto.decrypt(keypair.getPublic(), encrypted);

    assertEquals(text, decrypted);
  }


  /**
   * Test whether a private key is properly able to decrypt a text encrypted with the correspondent
   * public key
   * 
   * @throws CryptoException
   */
  @Test
  public void encryptPublicDecryptPrivate() throws CryptoException {
    Crypto crypto = new Crypto();
    KeyPair keypair = crypto.newKeyPair();

    String text = "to-be-encrypted";
    String encrypted = crypto.encrypt(keypair.getPublic(), text);
    String decrypted = crypto.decrypt(keypair.getPrivate(), encrypted);

    assertEquals(text, decrypted);
  }


  /**
   * Test crypto functions on protocol payloads
   * 
   * @throws SerializationException
   * @throws CryptoException
   */
  @Test
  public void encryptPayload() throws SerializationException, CryptoException {
    Crypto crypto = new Crypto();
    KeyPair keypair = crypto.newKeyPair();

    Payload payload = new Payload(keypair.getPublic(), new Frontier());
    String encrypted = crypto.encrypt(keypair.getPublic(), Serializer.serialize(payload));
    String decrypted = crypto.decrypt(keypair.getPrivate(), encrypted);

    Payload deserialized = (new Serializer<Payload>()).deserialize(decrypted);

    assertEquals(payload.getSenderId(), deserialized.getSenderId());
  }
}
