package distributedgroup_project2.application;

import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import distributedgroup_project2.CryptoUtils;
import distributedgroup_project2.PrivateKey;
import distributedgroup_project2.PublicKey;
import distributedgroup_project2.Utils;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;

public class ParticipantsManager {

	private final ContinuousSpace<Object> space;

	/**
	 * Constructor of the class ParticipantsManager.
	 *
	 * @param space: the continuous space where the new participants will be placed.
	 */
	public ParticipantsManager(ContinuousSpace<Object> space) {
		this.space = space;
	}

	/**
	 * Method to probabilistically spawn a new participant.
	 */
	@ScheduledMethod(start = 10, interval = 10)
	public void spawnParticipant() {
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		double spawnProbability = Utils.getParams().getDouble("spawn_probability");
		
		if (random <= spawnProbability) { 
			// Generate the new relay ID and key pair for encryption
			int id = Utils.generateNewRelayId();
			AsymmetricCipherKeyPair keyPair = CryptoUtils.generateKeyPair();

			// Create the relay and add it to the context
			PrivateKey privateKey = new PrivateKey(keyPair.getPrivate());
			PublicKey publicKey = new PublicKey(keyPair.getPublic());

			Participant participant = new Participant(this.space, id, publicKey, privateKey, false);
			
			Utils.getContext(this).add(participant);
			Utils.addParticipant(participant);
			
			Utils.log("New participant " + id + " added");
		}
	}

	/**
	 * Method to probabilistically kill an existing participant.
	 */
	@ScheduledMethod(start = 10, interval = 10)
	public void killParticipant() {
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		double killProbability = Utils.getParams().getDouble("kill_probability");

		if (random <= killProbability) {
			Context<Object> context = Utils.getContext(this);
			
			// Get all the existing relays in the context
			List<Participant> participants = context.stream()
					.filter(agent -> agent instanceof Participant)
					.map(agent -> (Participant) agent)
					.collect(Collectors.toList());
			
			if (participants.size() > 0) {
				// Take a random relay and remove it from the context
				int index = RandomHelper.nextIntFromTo(0, participants.size() - 1);
				Participant participant = participants.get(index);

				Utils.removeParticipant(index);
				context.remove(participant);
				Utils.log("Removed relay " + participant.getId());
			}
		}
	}
}
