package ds2.protocol.ssb;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;

import org.apache.tools.ant.taskdefs.Exit;

import ds2.application.ApplicationData;
import ds2.utility.logging.Logger;

/**
 * SSBLog is the class implementing the log functionality presented in the paper, 
 * section 2.2. It is identified by the public key of the identity that originates the events 
 * written in the list that it holds. The log is: secure (public key corresponds to a private key), 
 * monotonic, linear (total order), single writer, connected (no gaps in the sequence)
 * @author budge
 *
 */
public class SSBLog {

	// <id, events> ; event= <id, ...>
	private PublicKey publicKey; //public key of identity
	private ArrayList<SSBEvent> events; // list of events
	
	/**
	 * Class constructor, creates an empty log
	 * log <-- create(publicKey) 
	 * @param id
	 */
	public SSBLog(PublicKey publicKey) {
		this.publicKey = publicKey;
		this.events = new ArrayList<>();
	}
	
	/**
	 * Function to append one event originated locally. To be called only on the own log.
	 * log <-- log.append(content, privateKey)
	 * @param content
	 * @param privateKey
	 */
	public void appendLocal(ApplicationData<?> content, PrivateKey privateKey) {
		// Create the new event contents
		PublicKey idKey = this.publicKey;
		String previous = null;
		int index = this.getLastIndex() + 1;
		String signature = null;
		
		// If empty, leave null the previous message
		if (!this.events.isEmpty()) {
			previous = this.events.get(this.getLastIndex()).getSignature();
		}
		
		SSBEvent newEvent = new SSBEvent(idKey, previous, index, signature, content);
		String hashedEvent = this.getHashedEvent(newEvent);
		
		try {
			signature = this.sign(hashedEvent, privateKey);
		}
		catch (Exception ex) {
			System.err.println("Error while signing the hashed event");
			ex.printStackTrace();
			System.exit(-1);
		}

		newEvent.setSignature(signature);
		this.events.add(newEvent);
		// System.out.println("[SSBLog] New event generated: Index: " + newEvent.getIndex() + ", Content" + newEvent.getContent());
	}
	
	/**
	 * Extends log with events created remotely. 
	 * log <-- log.update(events)
	 * @param events
	 */
	public void update(ArrayList<SSBEvent> events) {
		this.events.addAll(events);
	}
	
	/**
	 * Get the set of events with index included between start and end (can be the same). 
	 * Indices start with 0 (e.g. if start == end == 0 -> one element, index 0). The sequence 
	 * number of SSBEvents should be equal to the index of the event in the log.
	 * log <--log.get(start, end)
	 * @param start
	 * @param end
	 * @return 
	 */
	public ArrayList<SSBEvent> getEvents(int start, int end){
		
		if (start > end) {
			// Actually not an error: when calling "getEventsSinceFrontier" this may happen.
//			System.out.println("log.getEvents(): Start index is greater than end! (The other Store has newer events for identity: "+ Logger.formatPort(this.publicKey) + ")");
		} else if (events.isEmpty()) {
			System.err.println("log.getEvents(): Log is empty for identity: "+ Logger.formatPort(this.publicKey));
		} else {
			ArrayList<SSBEvent> res = new ArrayList<>();
			for (int i = start; i <= end ; i++) {
				res.add(events.get(i));
			}
			return res;
		}
		return null;
	}
	
	/** 
	 * Get the index of the last event stored locally. 
	 * index <-- log.last
	 * @return
	 */
	// TODO Check if events is empty, and when doing operations with this function 
	// check if value is -1 (means no events stored yet)
	public int getLastIndex() {
		return events.size()-1;
	}
	
	/**
	 * Get the id (publicKey) of the log. 
	 * id <-- log.id
	 * @return
	 */
	public PublicKey getId() {
		return publicKey;
	}

	/**
	 * Help function that hashes an SSBEvent
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public String getHashedEvent(SSBEvent event) {
		byte[] hash = null;
		String result = null;
		
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {
			System.err.println("ERROR: Hashing algorithm not supported.");
			System.exit(0);
		}
		digest.reset();
		
		String originalString = event.getContents();
		hash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
		
		// Convert the byte array to HEXString format
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			hexString.append(Integer.toHexString(0xFF & hash[i]));
		}
		result = hexString.toString();
		
		return result;
	}
	
	/**
	 * Signs with an RSA-obtained privateKey a plainText
	 * @param hash
	 * @param privateKey
	 * @return the hash signed with the privateKey
	 * @throws Exception
	 */
	private String sign(String hash, PrivateKey privateKey) throws Exception {
		Signature privateSignature = Signature.getInstance("NONEwithRSA");
	    privateSignature.initSign(privateKey);
	    privateSignature.update(hash.getBytes(StandardCharsets.UTF_8));

	    byte[] signature = privateSignature.sign();

	    return Base64.getEncoder().encodeToString(signature);
	}

	/**
	 * Verifies that a previously created signature was created with a certain 
	 * RSA key using a plainText and a publicKey
	 * @param hash
	 * @param signature
	 * @param publicKey
	 * @return a true boolean whether the verification was successful, false otherwise 
	 * @throws Exception
	 */
	public boolean verifySign(String hash, String signature, PublicKey publicKey) throws Exception {
	    Signature publicSignature = Signature.getInstance("NONEwithRSA");
	    publicSignature.initVerify(publicKey);
	    publicSignature.update(hash.getBytes(StandardCharsets.UTF_8));

	    byte[] signatureBytes = Base64.getDecoder().decode(signature);

	    return publicSignature.verify(signatureBytes);
	}
	
}
