package bcastonly;

/**
 * Structure used to store messages in the out queue of a node, before
 * transmitting them
 * 
 * @author bortolan cosimo
 */
public class Message {
	private Relay recipient;
	private Perturbation perturbation;

	public Message(Relay recipient, Perturbation perturbation) {
		this.recipient = recipient;
		this.perturbation = perturbation;
	}

	public Relay getRecipient() {
		return recipient;
	}

	public Perturbation getPerturbation() {
		return perturbation;
	}

}
