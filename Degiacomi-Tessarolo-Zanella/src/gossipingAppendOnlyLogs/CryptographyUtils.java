package gossipingAppendOnlyLogs;

import gossipingAppendOnlyLogs.models.PersonKeys;
import gossipingAppendOnlyLogs.models.PersonPrivateKey;
import gossipingAppendOnlyLogs.models.PersonPublicKey;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CryptographyUtils {

    public static class DecryptionException extends Exception{}

    public static PersonKeys generateKeys() {
        try {
            var keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            return new PersonKeys(
                    new PersonPublicKey(keys.getPublic()),
                    new PersonPrivateKey(keys.getPrivate())
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("cannot generate key pair", e);
        }
    }

    public static String encrypt(Key encryptionKey, String message) {
        try {
            var rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.ENCRYPT_MODE, encryptionKey);
            var bytesEncrypted = rsa.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(bytesEncrypted);
        } catch (Exception e) {
            throw new RuntimeException("cannot encrypt message", e);
        }
    }

    public static String decrypt(Key decryptionKey, String encryptedMessage) throws DecryptionException {
        try {
            var decodedEncryptedMessage = Base64.getDecoder().decode(encryptedMessage);
            var rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.DECRYPT_MODE, decryptionKey);
            var utf8 = rsa.doFinal(decodedEncryptedMessage);
            return new String(utf8, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptionException();
        }
    }

}
