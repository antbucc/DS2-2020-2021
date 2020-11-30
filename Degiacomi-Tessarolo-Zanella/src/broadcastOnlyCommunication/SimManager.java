package broadcastOnlyCommunication;

import java.util.Random;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class SimManager {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	public final static Map<String, KeyPair> keyRing = new TreeMap<>();
	public final static List<String> topicsList = new ArrayList<>();
	/*
	 * probability that the simulation manager destroy or creates a new relay,
	 * respectively Value should be between 1 and 100
	 */
	private final int probRelayCrash;
	private final int probRelayreation;

	private final int minRelayCount;
	private final int maxRelayCount;

	private final int probPerturbationDrop;
	
	private final Random random = new Random();

	// the id for the next created relay
	protected int nextRelayId;

	public SimManager(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;

		Parameters params = RunEnvironment.getInstance().getParameters();
		this.probRelayCrash = params.getInteger("probCrash");
		this.probRelayreation = params.getInteger("probNew");
		this.minRelayCount = params.getInteger("minNumRelays");
		this.maxRelayCount = params.getInteger("maxNumRelays");
		this.probPerturbationDrop = params.getInteger("probPerturbationDrop");

		this.nextRelayId = params.getInteger("numRelays");
	}

	// for each station and relay, create a keypair
	public void initializeCrypto() {
		List<Relay> relays = Utils.getAllRelaysInGrid(grid, this);
		List<Station> stations = Utils.getAllStationsInGrid(grid, this);

		for (int i = 0; i < relays.size(); i++) {
			try {
				KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
				keyRing.put(relays.get(i).getRelayId(), keys);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < stations.size(); i++) {
			try {
				KeyPair keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
				keyRing.put(stations.get(i).getStationId(), keys);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}

	// create some topics
	public static void initializeTopics(int relaysCount) {
		int nTopics = (relaysCount / 3) * 2;		
		for (int i = 0; i < nTopics; i++) {
			String topic = UUID.randomUUID().toString();			
			topicsList.add(topic);
		}
	}

	// make a random relay crash indefinitely with probability p
	@ScheduledMethod(start = 1, interval = 100)
	public void makeCrash() {
		int hasCrashed = random.nextInt(100) + 1;

		if (hasCrashed <= probRelayCrash) {
			// get all relays
			List<Relay> relays = Utils.getAllRelaysInGrid(grid, this);
			if (!relays.isEmpty() && relays.size() > minRelayCount) {
				Relay relayToCrash = relays.remove(random.nextInt(relays.size()));
				System.out.println("Crashed a relay");
				var context = ContextUtils.getContext(relayToCrash);
				context.remove(relayToCrash);
			}
		}
	}

	// make a new relay appear with probability p
	@ScheduledMethod(start = 1, interval = 100)
	public void createNew() {
		int hasNew = random.nextInt(100) + 1;

		if (hasNew <= probRelayreation) {
			// get all relays
			List<Relay> relays = Utils.getAllRelaysInGrid(grid, this);
			if (relays.size() < maxRelayCount) {
				var id = "relay" + nextRelayId;
				nextRelayId++;
				Relay relay = Utils.instantiateCorrectRelayVersion(space, grid, id);
				@SuppressWarnings("unchecked")
				var context = (Context<Relay>) ContextUtils.getContext(this);
				context.add(relay);
				System.out.println("Created new " + id);

				NdPoint pt = space.getLocation(relay);
				grid.moveTo(relay, (int) pt.getX(), (int) pt.getY());
			}
		}
	}

	@ScheduledMethod(start = 1, interval = 10)
	public void dropPerturbation() {
		int wasDropped = random.nextInt(100) + 1;

		if (wasDropped <= probPerturbationDrop) {
			var perturbations = Utils.getAllPerturbationsInGrid(grid, this);
			if (!perturbations.isEmpty()) {
				var perturbationToDrop = perturbations.get(random.nextInt(perturbations.size()));
				var context = ContextUtils.getContext(perturbationToDrop);
				context.remove(perturbationToDrop);
				System.out.println("Dropped a perturbation");
			}
		}
	}

}
