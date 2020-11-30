package broadcastOnlyCommunication;

import java.util.HashMap;
import java.util.Map;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class RelayI extends Relay {
	private Map<String, Integer> frontier = new HashMap<String, Integer>();

	public RelayI(ContinuousSpace<Object> space, Grid<Object> grid, String id) {
		super(space, grid, id);
	}

	@Override
	public void processPerturbation(Perturbation p) {
		if (!frontier.containsKey(p.senderId)) {
			frontier.put(p.senderId, 0);
		}

		if (frontier.get(p.senderId).equals(p.ref)) {
			forwardPerturbation(p);
			frontier.put(p.senderId, nextRef(p));
			// observer notification of P.val goes here

		}
	}
}
