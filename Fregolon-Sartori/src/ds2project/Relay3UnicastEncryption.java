package ds2project;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.util.Base64;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

public class Relay3UnicastEncryption extends Relay3Unicast {
	private KeyPairGenerator keyGen;
	private KeyPair pair;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Cipher cipher;

	public Relay3UnicastEncryption(ContinuousSpace<Object> space, int id, Network<Object> graph) {
		super(space, id, graph);
		try {
			GenerateKeys(1024);
			cipher = Cipher.getInstance("RSA");
			createKeys();
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
			System.err.println("The selected encryption algorithm is not available");
			System.exit(1);
		}
	}

	@Override
	public void onSense(Perturbation p) {
		this.recordTotalPerturbation(p);
		if (p instanceof ARQ) {
			onSenseARQ(p);
		} else {
			if (log.get(p.getSrc()) == null)
				log.put(p.getSrc(), new LinkedList<>());
			LinkedList<Perturbation> node_log = log.get(p.getSrc());
			int next;
			if (node_log.isEmpty())
				next = 0;
			else
				next = next_ref(node_log.getLast());
			if (next == p.getRef()) {
				forward(p);
				node_log.add(p);
				try {
					decrypt(p.getVal());
					this.logPerturbation(p);
					this.updateAvgLatency(p.generationTick);
					nUpcall++;
				} catch (Exception e) {
					
				}
			}

		}

	}

	private void GenerateKeys(int keylength) throws NoSuchAlgorithmException, NoSuchProviderException {
		this.keyGen = KeyPairGenerator.getInstance("RSA");
		this.keyGen.initialize(keylength);
	}

	public void createKeys() {
		this.pair = this.keyGen.generateKeyPair();
		this.privateKey = pair.getPrivate();
		this.publicKey = pair.getPublic();
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public String encrypt(String message, PublicKey key)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return Base64.getEncoder().encodeToString((cipher.doFinal(message.getBytes("UTF-8"))));
	}

	public String decrypt(String encryptedMessage)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
		cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
		return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)), "UTF-8");
	}

	@Override
	public void scheduledSend() {
		if (!log.isEmpty()) {
			Random r = new Random();
			Relay3UnicastEncryption randomDestination = (Relay3UnicastEncryption) log.keySet().toArray()[r
					.nextInt(log.size())];
			String message = "Message" + lastRef;
			try {
				message = encrypt(message, randomDestination.getPublicKey());
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
					| UnsupportedEncodingException e) {
				e.printStackTrace();
				System.exit(1);
			}
			Perturbation p = new Perturbation(this, lastRef, message);
			lastRef++;
			forward(p);
		}
	}

}
