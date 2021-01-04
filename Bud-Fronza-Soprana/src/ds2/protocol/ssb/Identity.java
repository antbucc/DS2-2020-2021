package ds2.protocol.ssb;

import java.security.PrivateKey;
import java.security.PublicKey;

import ds2.utility.logging.Logger;

public class Identity {

	private PublicKey publicKey;
	private PrivateKey privateKey;

	public Identity(PublicKey publicKey, PrivateKey privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	
	public String toString() {
		return "Public key: " + Logger.formatPort(this.publicKey);
	}
	
}
