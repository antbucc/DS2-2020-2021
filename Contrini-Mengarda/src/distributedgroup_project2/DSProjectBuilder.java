package distributedgroup_project2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import distributedgroup_project2.application.Participant;
import distributedgroup_project2.application.ParticipantsManager;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.InfiniteBorders;
import repast.simphony.space.continuous.RandomCartesianAdder;

public class DSProjectBuilder implements ContextBuilder<Object> {

	private static final int SIZE = 50;
	private ContinuousSpace<Object> space;

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("distributedgroup_project2");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<>(),
				new InfiniteBorders<>(), SIZE, SIZE);

		int participantsCount = Utils.getParams().getInteger("participants_count");
		String friendsDataFile = Utils.getParams().getString("friends_data_file");

		// Read the data file and build the friends network.
		Map<Integer, Set<Integer>> friends = new HashMap<>();
		
		// Read friendship data only if transitive-interest gossip is selected 
		// and a file name is provided.
		String gossipType = Utils.getParams().getString("gossip_type");
		if (!gossipType.equals("OpenGossip") && !friendsDataFile.isEmpty()) {
			friends = readFriendsFromFile(friendsDataFile, participantsCount);
		}

		// Generate participants in an amount defined by the runtime parameter.
		for (int i = 0; i < participantsCount; i++) {
			// Each participant is assigned an ID corresponding to the loop index (0..N-1).
			AsymmetricCipherKeyPair keyPair = CryptoUtils.generateKeyPair();
			PrivateKey privateKey = new PrivateKey(keyPair.getPrivate());
			PublicKey publicKey = new PublicKey(keyPair.getPublic());

			context.add(new Participant(space, i, publicKey, privateKey, true));
		}

		// Sort participants by ID so that participants are better organized when
		// building the topology.
		List<Participant> participants = context.stream().map(p -> (Participant) p).collect(Collectors.toList());
		Collections.sort(participants, (x, y) -> x.getId() - y.getId());

		// Place participants in the space according to the chosen topology.
		String topology = Utils.getParams().getString("topology");
		createTopology(topology, participants);

		// When the topology is random, add a participant manager that keeps adding/removing
		// participants randomly.
		if (topology.equals("random")) {
			context.add(new ParticipantsManager(space));
		}

		Utils.setParticipants(participants);

		// Build a network to represent follow relationships
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<>("friends", context, true);
		netBuilder.buildNetwork();

		// Schedule a call to initFriends at tick 0 to initialize
		// the stores/logs with follows on each participant
		ScheduleParameters params = ScheduleParameters.createOneTime(0);
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		schedule.schedule(params, this, "initFriends", participants, friends);
		
		return context;
	}
	
	/**
	 * Method to initialize the follow relationships on participants and the network.
	 * 
	 * @param participants: the list of participants in the context;
	 * @param friendsIds: a map participantID -> {friendsIDs} that is used to fill the network.
	 */
	public void initFriends(List<Participant> participants, Map<Integer, Set<Integer>> friendships) {
		for (Map.Entry<Integer, Set<Integer>> entry : friendships.entrySet()) {
			int participantId = entry.getKey();
			
			// Find all the Participant instances corresponding to the given IDs
			Set<Participant> friends = entry.getValue().stream().map(id -> participants.get(id))
					.collect(Collectors.toSet());
			
			// Tell the participant to follow its friends
			participants.get(participantId).initFriends(friends);
		}
	}
	
	/**
	 * Method to read friendship data from the given file and build a map participantID -> { friendsIDs }.
	 * Each line in the file must represent an unidirectional follow between two participants.
	 * e.g. "10 6" means that 10 is following 6
	 * 
	 * @param filename: the name of the input file to read;
	 * @param participantsCount the number of participants to extract from the input file;
	 * @return A Map between a participant ID and a set of IDs that the participant is following.
	 */
	private Map<Integer, Set<Integer>> readFriendsFromFile(String filename, int participantsCount) {
		Map<Integer, Set<Integer>> friends = new HashMap<>();
		Map<Integer, Set<Integer>> friendsWithRemappedIds = new HashMap<>();
		Map<Integer, Integer> idsMapper = new HashMap<>();

		File file = new File(filename);
		Scanner sc = null;

		// Check if the input file exists
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			Utils.logError("File " + filename + " does not exist");
			System.exit(1);
		}

		int index = 0;
		while (sc.hasNextLine()) {
			// Read a line from the file and get the two IDs, separated by a space
			String[] input = sc.nextLine().trim().split(" ");

			if (input.length == 2) {
				int first = Integer.parseInt(input[0]);
				int second = Integer.parseInt(input[1]);

				// Since the file could contain arbitrary relay IDs,
				// we store a mapping with a new ID (index) starting from 0
				if (!idsMapper.containsKey(first)) {
					idsMapper.put(first, index++);
				}
				
				if (!idsMapper.containsKey(second)) {
					idsMapper.put(second, index++);
				}

				// We add the follow relationship to the map
				friends.putIfAbsent(first, new HashSet<>());
				friends.get(first).add(second);
			}
		}
		
		sc.close();
		
		// Now that we've read the whole file, we need to do another pass 
		// in order to keep only a subset of `participantsCount` participants,
		// and to remap relay IDs.
		
		index = 0;
		for (Map.Entry<Integer, Set<Integer>> entry : friends.entrySet()) {
			int newId = idsMapper.get(entry.getKey());
			
			// Consider this relay only if its ID doesn't exceed `participantsCount`
			if (newId < participantsCount) {
				// Since the Map enumeration doesn't follow any order,
				// we also need to check whether we've already considered `participantsCount` items
				if (index++ >= participantsCount - 1) {
					// If yes, stop here, we're done
					break;
				}
				
				// We need to keep this participant, let's add it to the new set 
				friendsWithRemappedIds.put(newId, new HashSet<>());
	
				// Also add "friends"...
				entry.getValue().forEach(f -> {
					// ...but only if they're part of the subset
					// (otherwise we would have friends that don't exist!)
					if (idsMapper.get(f) < participantsCount) {
						friendsWithRemappedIds.get(newId).add(idsMapper.get(f));					
					}
				});
			}
		}

		return friendsWithRemappedIds;
	}

	/**
	 * Method to arrange the participants in a specific topology. The topology is
	 * specified at runtime through a parameter.
	 * 
	 * @param participants: List of participants sorted by (relay) ID.
	 */
	private void createTopology(String topology, List<Participant> participants) {
		switch (topology) {
			case "random":
				// By default participants are positioned randomly so we don't need to do anything
				break;
			case "ring":
				// Place the participants in a ring with the defined radius.
				double ringRadius = SIZE * 0.4;
				makeRing(participants, ringRadius);
				break;
			case "star":
				// Place a participant in the middle and the other participants in a ring
				double starRadius = SIZE * 0.4;
				makeRing(participants.subList(1, participants.size()), starRadius);
	
				createCenter(participants);
				break;
			case "star+":
				// Place a participant in the middle and the others in two rings with the defined radii
				double innerRadius = SIZE * 0.2;
				makeRing(participants.subList(1, participants.size() / 3), innerRadius);
	
				double outerRadius = SIZE * 0.4;
				makeRing(participants.subList(participants.size() / 3, participants.size()), outerRadius);
	
				createCenter(participants);
				break;
		}

	}

	/**
	 * Method to arrange the participants in a ring topology.
	 * 
	 * @param participants: a list of participants ordered by ID;
	 * @param radius: the radius of the ring topology.
	 */
	private void makeRing(List<Participant> participants, double radius) {
		// Divide 360 by the number of participants to get the number of
		// degrees that each relay should be separated by.
		double alpha = Math.toRadians((360.0 / participants.size()));

		for (int i = 0; i < participants.size(); i++) {
			Participant participant = participants.get(i);

			// Compute the X and Y shift from the center of the circle.
			double x = radius * Math.cos(i * alpha);
			double y = radius * Math.sin(i * alpha);

			// Place the participant in the calculated position
			space.moveTo(participant, SIZE / 2 + x, SIZE / 2 + y);
		}
	}

	/**
	 * Method to set the participant with ID 0 to the center of the screen.
	 * 
	 * @param participants: a list of participants ordered by ID.
	 */
	private void createCenter(List<Participant> participants) {
		Participant center = participants.get(0);
		space.moveTo(center, SIZE / 2, SIZE / 2);
	}
}
