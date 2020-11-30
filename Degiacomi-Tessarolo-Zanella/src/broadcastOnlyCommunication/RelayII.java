package broadcastOnlyCommunication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.essentials.RepastEssentials;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class RelayII extends Relay {
	private Map<String, Integer> frontier = new HashMap<String, Integer>();
	private ArrayList<Perturbation> bag = new ArrayList<Perturbation>();

	public RelayII(ContinuousSpace<Object> space, Grid<Object> grid, String id) {
		super(space, grid, id);
	}

	@Override
	public void processPerturbation(Perturbation p) {
		GridPoint pt = grid.getLocation(this);
		Double tick = RepastEssentials.GetTickCount();
		Boolean noElementsToDeliver = false;
		System.out.println(tick + " -- Relay " + pt.getX() + " - " + pt.getY() + ": " + p.val);

		if (!frontier.containsKey(p.senderId)) {
			frontier.put(p.senderId, 0);
		}

		// if the perturbation has a ref number higher than expected, put it in the bag
		// because it is getting an OOO delivery
		if (p.ref >= frontier.get(p.senderId) && !bag.contains(p)) {
			// add the new perturbation to the bag, even if it can be forwarded immediately
			bag.add(p);

			// cycle through the list to find out if one or more elements can be forwarded
			while (!noElementsToDeliver) {
				noElementsToDeliver = true;

				for (int i = 0; i < bag.size(); i++) {
					Perturbation bagPt = bag.get(i);

					// an element can be forwarded
					if (frontier.get(bagPt.senderId).equals(bagPt.ref)) {
						forwardPerturbation(bagPt);
						frontier.put(bagPt.senderId, nextRef(bagPt));
						bag.remove(bagPt);
						// it could cause something else to be forwarded too
						noElementsToDeliver = false;
					}
				}
			}
		}
	}
}