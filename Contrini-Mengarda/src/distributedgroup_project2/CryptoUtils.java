package distributedgroup_project2;

import java.security.SecureRandom;
import java.util.Base64;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.signers.Ed25519Signer;

public class CryptoUtils { 
	
	/**
	 * Method to generate a new Ed25519 key pair.
	 * 
	 * @return: the generated key pair.
	 */
	public static AsymmetricCipherKeyPair generateKeyPair() {
		try {
			Ed25519KeyPairGenerator pair = new Ed25519KeyPairGenerator();
			pair.init(new KeyGenerationParameters(new SecureRandom(), 256));
			return pair.generateKeyPair();
		} catch (Exception e) {
			Utils.logError("Error while generating Ed25519 keypair");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method to sign the given string using the Ed25519 private key.
	 * 
	 * @param privateKey: the private key to use to sign the content;
	 * @param content: the string to be signed;
	 * @return: the Base64 encoded representation of the signature.
	 */
	public static String sign(PrivateKey privateKey, String content) {	    
	    Signer signer = new Ed25519Signer();
	    signer.init(true, privateKey.getKey());
        signer.update(content.getBytes(), 0, content.length());
        
        byte[] signature;
		try {
			signature = signer.generateSignature();
		} catch (DataLengthException | CryptoException e) {
			Utils.logError("Cannot sign the payload");
			return null;
		}
		
	    return Base64.getEncoder().encodeToString(signature);
	}

	/**
	 * Method to verify whether a signature is valid or not using Ed25519.
	 * 
	 * @param publicKey: the public key used to verify the signature;
	 * @param signature: the signature that needs to be checked;
	 * @param content: the original content of the signature;
	 * @return: true if the signature is correct, false otherwise.
	 */	
	public static boolean verify(PublicKey publicKey, String signature, String content) {
		Signer verifier = new Ed25519Signer();
	    verifier.init(false, publicKey.getKey());
	    verifier.update(content.getBytes(), 0, content.length());
        return verifier.verifySignature(Base64.getDecoder().decode(signature));
	}
}
