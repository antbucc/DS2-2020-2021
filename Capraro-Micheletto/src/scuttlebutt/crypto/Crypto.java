package scuttlebutt.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import scuttlebutt.configuration.ScuttlebuttConfiguration;
import scuttlebutt.exception.CryptoException;
import scuttlebutt.exception.SerializationException;
import scuttlebutt.serialization.Serializer;

/**
 * Provide basic crypto functionalities needed by the Scuttlebutt protocol by implementing a
 * symmetric encryption where the shared secret is exchanged through a private-public key
 * infrastructure
 */
public class Crypto {

  private static final Logger LOGGER = Logger.getLogger(Crypto.class.getName());

  /** Asymmetric key bit size */
  private static final int RSA_KEY_SIZE = 2048;

  /** Symmetric key bit size */
  private static final int AES_KEY_SIZE = 128;

  /** Asymmetric encryption algorithm */
  private static final String KEY_GENERATION_ALGORITHM = "RSA";

  /** RSA mode */
  private static final String RSA = "RSA/ECB/PKCS1Padding";

  /** Generator used to generate public and private keys */
  private KeyPairGenerator keyPairGenerator;


  public Crypto() {
    try {
      keyPairGenerator = KeyPairGenerator.getInstance(KEY_GENERATION_ALGORITHM);
      keyPairGenerator.initialize(RSA_KEY_SIZE, ScuttlebuttConfiguration.SECURE_RANDOM);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }


  /**
   * Generate a KeyPair from the KeyPairGenerator
   * 
   * @return the newly generated KeyPair
   */
  public KeyPair newKeyPair() {
    return keyPairGenerator.generateKeyPair();
  }


  /**
   * Generate the AES secret to be used as symmetric key
   * 
   * @return the newly generated AES SecretKey
   */
  private SecretKey generateSymmetric() {
    KeyGenerator generator;
    try {
      generator = KeyGenerator.getInstance("AES");
      generator.init(AES_KEY_SIZE, ScuttlebuttConfiguration.SECURE_RANDOM);
      return generator.generateKey();
    } catch (NoSuchAlgorithmException e) {
      LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
    return null;
  }


  /**
   * Generate a secret symmetric and encrypt the provided string with it. Encrypt the secret key
   * with the provided private key. Serialize the encrypted key and text
   * 
   * @param key: the public key used to encrypt the shared secret
   * @param text: the plain string to be encrypted
   * @return the serialization of the secret encrypted with the private key and the text encrypted
   *         with the secret
   * @throws CryptoException
   */
  public String encrypt(Key key, String text) throws CryptoException {
    try {
      SecretKey secretKey = generateSymmetric();

      // Encrypt payload using symmetric key
      Cipher aesCipher = Cipher.getInstance("AES");
      aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] encryptedTextBytes = aesCipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

      // Encrypt symmetric key using asymmetric key
      Cipher rsaCipher = Cipher.getInstance(RSA);
      rsaCipher.init(Cipher.ENCRYPT_MODE, key);
      String secretKeySerialized = Serializer.serialize(secretKey);
      byte[] encryptedKeyBytes =
          rsaCipher.doFinal(secretKeySerialized.getBytes(StandardCharsets.UTF_8));


      EncryptedPayload encryptedPayload =
          new EncryptedPayload(encryptedKeyBytes, encryptedTextBytes);
      return Serializer.serialize(encryptedPayload);
    } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
        | IllegalBlockSizeException | BadPaddingException | SerializationException e) {
      // LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
      throw new CryptoException(e);
    }
  }


  /**
   * Deserialize the text to extract the shared secret and the text, decrypt the shared secret with
   * the provided key and the text with the shared secret
   * 
   * @param key: the private key used to decrypt the shared secret
   * @param text: the string to be deserialized and decrypted
   * @return the plain text decrypted, or the original text if it is not encrypted
   * @throws CryptoException
   */
  public String decrypt(Key key, String text) throws CryptoException {
    EncryptedPayload encryptedPayload = null;
    SecretKey secretKey;

    try {
      // Deserialize encrypted payload
      encryptedPayload = (new Serializer<EncryptedPayload>()).deserialize(text);

      // Check if the message is actually encrypted
      if (encryptedPayload.secretKey == null || encryptedPayload.secretKey.length == 0) {
        return new String(encryptedPayload.payload, StandardCharsets.UTF_8);
      }

      // Decrypt symmetric key using asymmetric key
      Cipher rsaCipher = Cipher.getInstance(RSA);
      rsaCipher.init(Cipher.DECRYPT_MODE, key);
      byte[] decryptedSymmetricKeyBytes = rsaCipher.doFinal(encryptedPayload.secretKey);
      String decryptedSymmetricKey = new String(decryptedSymmetricKeyBytes, StandardCharsets.UTF_8);
      secretKey = (new Serializer<SecretKeySpec>()).deserialize(decryptedSymmetricKey);

      // Decrypt payload using symmetric key
      Cipher aesCipher = Cipher.getInstance("AES");
      aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
      byte[] decryptedPayload = aesCipher.doFinal(encryptedPayload.payload);

      return new String(decryptedPayload, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      // LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
      LOGGER.log(Level.SEVERE, encryptedPayload.secretKey.toString());
      throw new CryptoException(e);
    } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
        | NoSuchAlgorithmException | NoSuchPaddingException | SerializationException e) {
      // LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
      throw new CryptoException(e);
    }
  }

}


