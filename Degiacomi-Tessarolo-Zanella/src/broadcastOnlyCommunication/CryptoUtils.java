package broadcastOnlyCommunication;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

public class CryptoUtils {
	
	public static String decryptMessage(String encryptedMessage, PrivateKey privateKey) throws Exception {
		byte[] decode = Base64.getDecoder().decode(encryptedMessage);
		Cipher rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] utf8 = rsa.doFinal(decode);
		return new String(utf8, "UTF8");
	}
	
	public static String encryptMessage(String plainMessage, PublicKey pubKey) throws Exception {
		Cipher rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] bytesEncr = rsa.doFinal(plainMessage.getBytes());
		return Base64.getEncoder().encodeToString(bytesEncr);
	}

}
