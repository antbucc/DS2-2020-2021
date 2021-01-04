package distributedgroup_project2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import distributedgroup_project2.application.Participant;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

public class Utils {
	public static final int MAX_PERTURBATION_RADIUS = 25;

	private static List<Participant> participants = new ArrayList<>();

	/**
	 * Method to set the list of participants. It is used to initialize the simulation.
	 * 
	 * @param participants: the list of participants.
	 */
	public static void setParticipants(List<Participant> participants) {
		Utils.participants = participants;
	}

	/**
	 * Method to generate a new ID for a new relay.
	 * 
	 * @return: the new ID.
	 */
	public static int generateNewRelayId() {
		return participants.size();
	}

	/**
	 * Returns the context for an object.
	 * 
	 * @param obj: the object (agent instance) to find the context for;
	 * @return: the context the object belongs to.
	 */
	@SuppressWarnings("unchecked")
	public static Context<Object> getContext(Object obj) {
		Context<Object> context = ContextUtils.getContext(obj);
		return context;
	}

	/**
	 * Returns the current tick of the simulation, during execution.
	 * 
	 * @return: the current tick.
	 */
	public static double getCurrentTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}

	/**
	 * Returns the runtime Repast parameters of the simulation.
	 * 
	 * @return: the parameters of the simulation.
	 */
	public static Parameters getParams() {
		return RunEnvironment.getInstance().getParameters();
	}

	/**
	 * Method to retrieve the public key of a participant given its relay ID.
	 * 
	 * @param id: the ID of the participant;
	 * @return: the public key of the participant.
	 */
	public static PublicKey getPublicKeyByParticipantId(int id) {
		return participants.get(id).getPublicKey();
	}

	/**
	 * Method to add a new participant to the participants list.
	 * 
	 * @param participant: the new participant.
	 */
	public static void addParticipant(Participant participant) {
		participants.add(participant);
	}

	/**
	 * Method to remove a participant from the participants list given the relay ID.
	 * 
	 * @param index: the ID of the participant.
	 */
	public static void removeParticipant(int id) {
		// We don't actually remove the slot in the array in order to
		// keep the association between the array index and the relay ID 
		participants.set(id, null);
	}

	/**
	 * Method to retrieve a participant given its public key.
	 * 
	 * @param key: the key of the participant;
	 * @return: the Participant associated with that public key, or null if not found.
	 */
	public static Participant getParticipantByPublicKey(PublicKey key) {
		for (Participant p : participants) {
			if (p != null && p.getPublicKey().equals(key)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * Method to pick a random existing participant from a set of participants.
	 * 
	 * @param set: the set of participants;
	 * @return: a randomly picked participant (null if no existing participants are available).
	 */
	public static Participant getRandomParticipant(Set<PublicKey> set) {
		if (set.size() == 0) {
			return null;
		}

		List<Participant> existingParticipants = new ArrayList<>();

		// Filter out participants that are not in the system anymore
		set.forEach(pk -> {
			Participant p = getParticipantByPublicKey(pk);
			if (p != null) {
				existingParticipants.add(p);
			}
		});

		if (existingParticipants.size() == 0) {
			return null;
		}
		
		// Pick a random participant from the remaining ones
		return existingParticipants.get(RandomHelper.nextIntFromTo(0, existingParticipants.size() - 1));
	}

	/**
	 * Method to retrieve the "friends" network from the context.
	 * 
	 * @param obj: the agent to be used to retrieve the context;
	 * @return: the "friends" network.
	 */
	@SuppressWarnings("unchecked")
	public static Network<Object> getNetwork(Object obj) {
		return (Network<Object>) Utils.getContext(obj).getProjection("friends");
	}

	/**
	 * Method to log information in console, along with the current tick.
	 * 
	 * @param message: the message to be logged;
	 */
	public static void log(String message) {
		System.out.println("[" + (int) Utils.getCurrentTick() + "] " + message);
	}

	/**
	 * Method to log errors in console, along with the current tick.
	 * 
	 * @param message: the message to be logged;
	 */
	public static void logError(String message) {
		System.err.println("[" + (int) Utils.getCurrentTick() + "] " + message);
	}

}
