package ssb_broadcast.utils;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.Utils;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import repast.simphony.util.collections.Pair;

public class CryptoUtil {
	public static Pair<String, String> generateKeyPair() {
		try {
			int keySize = 256;
			SecureRandom random = new SecureRandom();
			KeyPairGenerator keyPairGenerator = new KeyPairGenerator();
			
			keyPairGenerator.initialize(keySize, random);
			
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			
			byte[] encodedPublicKey = keyPair.getPublic().getEncoded();
			byte[] encodedPrivateKey = keyPair.getPrivate().getEncoded();
			
			String hexPublicKey = Utils.bytesToHex(encodedPublicKey);
			String hexPrivateKey = Utils.bytesToHex(encodedPrivateKey);
			
			return new Pair<String, String>(hexPublicKey, hexPrivateKey);
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException();
		}
	}

	public static byte[] sign(String hexPrivateKey, byte[] input) {
		try {
			PKCS8EncodedKeySpec privateKeyEncoded = new PKCS8EncodedKeySpec(Utils.hexToBytes(hexPrivateKey));
			
			EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
			Signature signature = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
			PrivateKey privateKey = new EdDSAPrivateKey(privateKeyEncoded);
			
			signature.initSign(privateKey);
			signature.update(input);
			
			return signature.sign();
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException();
		}
	}
	
	public static boolean verify(String hexPublicKey, byte[] input, byte[] sign) {
		try {
			X509EncodedKeySpec encodedPublicKey = new X509EncodedKeySpec(Utils.hexToBytes(hexPublicKey));
			
			EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
			Signature signature = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
			PublicKey publicKey = new EdDSAPublicKey(encodedPublicKey);
			
			signature.initVerify(publicKey);
			signature.update(input);
			
			return signature.verify(sign);
		} catch (Exception ex) {
			ex.printStackTrace();
			
			throw new RuntimeException();
		}
	}
}
