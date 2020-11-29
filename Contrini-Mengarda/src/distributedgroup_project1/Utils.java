package distributedgroup_project1;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

public class Utils {
	public static final int MAX_PERTURBATION_RADIUS = 25;
	
	// Number of relays in the space, fixed at the beginning of the execution from runtime parameters
	private static int relaysCount;
	
	// Ordered collection of public keys, one per relay
	private static List<RSAPublicKey> publicKeys;
	
	/**
	 * Method to set the number of relays that were created in the context.
	 * 
	 * @param newRelaysCount: the number of relays
	 */
	public static void setRelaysCount(int newRelaysCount) {
		relaysCount = newRelaysCount;
	}
	
	/**
	 * Method to get the number of relays that were created in the context.
	 * 
	 * @return: the number of relays
	 */
	public static int getRelaysCount() {
		return relaysCount;
	}
	
	/**
	 * Method to retrieve the context of an object.
	 * 
	 * @param obj: the object (agent) to find the context for
	 * @return: the context the object belongs to
	 */
	public static Context<Object> getContext(Object obj) {
		@SuppressWarnings("unchecked")
		Context<Object> context = ContextUtils.getContext(obj);
		return context;
	}
	
	/**
	 * Method to retrieve the current tick of the simulation, at runtime.
	 * 
	 * @return: the current tick
	 */
	public static double getCurrentTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
	
	/**
	 * Method to retrieve the runtime Repast parameters of the simulation.
	 * 
	 * @return: the parameters of the simulation
	 */
	public static Parameters getParams() {
		return RunEnvironment.getInstance().getParameters();
	}
	
	/**
	 * Method to randomly pick a relay.
	 * 
	 * @return: the ID of the chosen relay
	 */
	public static int getRandomRelayId() {
		return RandomHelper.nextIntFromTo(0, relaysCount - 1);
	}
	
	/**
	 * Method to randomly generate a topic ID to be used in the PubSub multicast.
	 * 
	 * @return: the ID of the topic, represented by a negative integer
	 */
	public static int getRandomTopic() {
		return RandomHelper.nextIntFromTo(1, 10);
	}
	
	/**
	 * Method to set the list of public keys for the relay
	 * 
	 * @param newPublicKeys
	 */
	public static void setRelaysPublicKeys(List<RSAPublicKey> newPublicKeys) {
		publicKeys = newPublicKeys;
	}

	/**
	 * Method to save a new public key for a new relay
	 * 
	 * @param newPublicKey
	 */
	public static void addRelayPublicKey(RSAPublicKey newPublicKey) {
		publicKeys.add(newPublicKey);
	}
	
	/**
	 * Method to obtain the public key of a relay given the ID
	 * 
	 * @param relayId: the ID for which the public key is requested
	 * @return the public key corresponding to the relay
	 */
	public static RSAPublicKey getPublicKeyByRelayId(int relayId) {
		return publicKeys.get(relayId);
	}
	
	/**
	 * Encrypts a message with the given RSA public key
	 * 
	 * @param message: message to encrypt
	 * @param key: public key to use for encryption
	 * @return the encrypted message encoded in base64
	 */
	public static String encrypt(String message, RSAPublicKey key) {
		try {
			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] bytes = message.getBytes();
			return Base64.getEncoder().encodeToString(rsaCipher.doFinal(bytes));
		} catch (Exception e) {
			System.err.println("Error while encrypting");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Decrypts a message with the given RSA private key.
	 * 
	 * @param message: the base64-encoded message to decrypt
	 * @param key: private key to use for decryption
	 * @return the decrypted message as byte[]
	 */
	public static byte[] decrypt(String message, RSAPrivateKey key) {
		try {
			Cipher rsaCipher = Cipher.getInstance("RSA");
			rsaCipher.init(Cipher.DECRYPT_MODE, key);
			byte[] bytes = Base64.getDecoder().decode(message);
			return rsaCipher.doFinal(bytes);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.err.println("Error while initializing RSA cipher");
			e.printStackTrace();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// This is not the right key
		}
		return null;
	}
	
	/**
	 * Generates a new RSA key pair
	 * 
	 * @return the generated key pair
	 */
	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keyGen.initialize(1024, random);
			return keyGen.generateKeyPair();
		} catch (Exception e) {
			System.err.println("Error while generating RSA keypair");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Generates a random string with the given length, composed of letters and numbers
	 * 
	 * @param length
	 * @return generated string
	 */
	public static String getRandomString(int length) { 
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz"; 
        StringBuilder sb = new StringBuilder(length); 
        for (int i = 0; i < length; i++) { 
            int index = RandomHelper.nextIntFromTo(0, alphabet.length() - 1);
            sb.append(alphabet.charAt(index)); 
        } 
        return sb.toString(); 
    } 
}
