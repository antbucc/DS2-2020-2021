package distributedgroup_project1;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.InfiniteBorders;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;

public class DSProjectBuilder implements ContextBuilder<Object> {

	private static final int SIZE = 50;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("distributedgroup_project1");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<>(),
				new InfiniteBorders<>(), SIZE, SIZE);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new repast.simphony.space.grid.InfiniteBorders<>(), new SimpleGridAdder<Object>(), true, SIZE, SIZE));

		// Generate relays in an amount defined by the runtime parameter 
		int relaysCount = Utils.getParams().getInteger("relays_count");
		
		List<RSAPublicKey> publicKeys = new ArrayList<>();
		
		for (int i = 0; i < relaysCount; i++) {
			// Generate an RSA key pair for each relay, for the P2P mode.
			// Public keys are stored in a "global" list, so that they can be obtained by other relays,
			// while private keys are private fields inside each relay
			KeyPair keys = Utils.generateKeyPair();
			publicKeys.add((RSAPublicKey) keys.getPublic());
			
			// Add the relay to the context/space.
			// Each relay is assigned an ID corresponding to the loop index (0..N-1)
			context.add(new Relay(space, grid, i, (RSAPrivateKey) keys.getPrivate()));
		}

		// Sort relays by ID so that relays are better organized when building the topology
		List<Object> relays = new ArrayList<Object>(context);
		Collections.sort(relays, (x, y) -> ((Relay) x).getId() - ((Relay) y).getId());
		
		// Place relays in the space/grid according to the chosen topology
		String topology = Utils.getParams().getString("topology");
		createTopology(topology, relays);
		
		// When the topology is random add a relay manager that keeps adding/removing relays randomly
		if (topology.equals("random")) {
			context.add(new RelaysManager(space, grid));
		}

		Utils.setRelaysCount(relaysCount);
		Utils.setRelaysPublicKeys(publicKeys);
		
		return context;
	}

	/**
	 * Method to arrange the relays in a specific topology.
	 * The topology is specified at runtime through a parameter.
	 * 
	 * @param relays: List of relays sorted by relay ID
	 */
	private void createTopology(String topology, List<Object> relays) {
		switch (topology) {
			case "random":
				// By default relays are positioned randomly, so we just need
				// to move them on the grid
				for (Object obj : relays) {
					NdPoint pt = space.getLocation(obj);
					grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
				}
				break;
			case "ring":
				// Place the relays in a ring with the defined radius
				double ringRadius = SIZE * 0.4;
				makeRing(relays, ringRadius);
				break;
			case "star":
				// Place a relay in the middle and the other relays in a ring
				double starRadius = SIZE * 0.4;
				makeRing(relays.subList(1, relays.size()), starRadius);
	
				createCenter(relays);
				break;
			case "star+":
				// Place a relay in the middle and the others in two rings with the defined radiuses
				double innerRadius = SIZE * 0.2;
				makeRing(relays.subList(1, relays.size() / 3), innerRadius);
	
				double outerRadius = SIZE * 0.4;
				makeRing(relays.subList(relays.size() / 3, relays.size()), outerRadius);
	
				createCenter(relays);
				break;
			case "tree":
				break;
		}

	}

	/**
	 * Method to arrange the relays in a ring topology.
	 * 
	 * @param relays: a list of relays ordered by ID
	 * @param radius: the radius of the ring topology
	 */
	private void makeRing(List<Object> relays, double radius) {
		// We divide 360 degrees by the number of relays to get the number of degrees
		// that each relay should be separated by
		double alpha = Math.toRadians((360.0 / relays.size()));

		for (int i = 0; i < relays.size(); i++) {
			Relay relay = (Relay) relays.get(i);
			
			// Compute the X and Y shift from the center of the circle
			double x = radius * Math.cos(i * alpha);
			double y = radius * Math.sin(i * alpha);
			
			// Place the relay in the calculated position
			space.moveTo(relay, SIZE / 2 + x, SIZE / 2 + y);
			NdPoint pt = space.getLocation(relay);
			grid.moveTo(relay, (int) pt.getX(), (int) pt.getY());
		}
	}

	/**
	 * Method to set the position of the relay with ID 0 to the center of the screen.
	 * 
	 * @param relays: a list of relays ordered by ID
	 */
	private void createCenter(List<Object> relays) {
		Relay center = (Relay) relays.get(0);
		space.moveTo(center, SIZE / 2, SIZE / 2);
		NdPoint pt = space.getLocation(center);
		grid.moveTo(center, (int) pt.getX(), (int) pt.getY());
	}
}
