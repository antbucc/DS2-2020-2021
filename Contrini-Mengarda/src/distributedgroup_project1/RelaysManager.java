package distributedgroup_project1;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.stream.Collectors;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;

public class RelaysManager {

	private final ContinuousSpace<Object> space;
	private final Grid<Object> grid;

	public RelaysManager(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}

	@ScheduledMethod(start = 10, interval = 10)
	public void spawnRelay() {
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		double spawnProbability = Utils.getParams().getDouble("spawn_probability");
		
		if (random <= spawnProbability) { 
			// Generate the new relay ID and key pair for encryption
			int id = Utils.getRelaysCount();
			KeyPair keys = Utils.generateKeyPair();
			Utils.addRelayPublicKey((RSAPublicKey) keys.getPublic());
			
			// Create the relay and add it to the context
			Relay relay = new Relay(space, grid, id, (RSAPrivateKey) keys.getPrivate());
			
			Utils.getContext(this).add(relay);
			Utils.setRelaysCount(id + 1);
			
			// Move the relay in the space
			NdPoint pt = space.getLocation(relay);
			grid.moveTo(relay, (int) pt.getX(), (int) pt.getY());
			
			System.out.println("New relay " + id + " added");
		}
	}

	@ScheduledMethod(start = 10, interval = 10)
	public void killRelay() {
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		double killProbability = Utils.getParams().getDouble("kill_probability");

		if (random <= killProbability) {
			Context<Object> context = Utils.getContext(this);
			
			// Get all the existing relays in the context
			List<Relay> relays = context.stream()
					.filter(agent -> agent instanceof Relay)
					.map(agent -> (Relay) agent)
					.collect(Collectors.toList());
			
			if (relays.size() > 0) {
				// Take a random relay and remove it from the context
				Relay relay = relays.get(RandomHelper.nextIntFromTo(0, relays.size() - 1));
				context.remove(relay);
				System.out.println("Removed relay " + relay.getId());
			}
		}
	}

}
