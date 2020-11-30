package broadcastOnlyCommunication;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class ProjectBuilder implements ContextBuilder<Object> {
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("BroadcastOnlyCommunication");
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 50, 50);
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 50, 50));

		Parameters params = RunEnvironment.getInstance().getParameters();

		int stationCount = params.getInteger("numStations");
		for (int i = 0; i < stationCount; i++) {
			var id = "station" + i;
			Station station = Utils.instantiateCorrectStationVersion(space, grid, id);
			context.add(station);
		}
		int relayCount = params.getInteger("numRelays");
		SimManager.initializeTopics(relayCount);
		for (int i = 0; i < relayCount; i++) {
			var id = "relay" + i;
			Relay relay = Utils.instantiateCorrectRelayVersion(space, grid, id);			
			context.add(relay);
		}
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}

		// create a SimManager agent
		SimManager manager = new SimManager(space, grid);
		context.add(manager);
		grid.moveTo(manager, 0, 0);
		
		manager.initializeCrypto();
		
		int maxTicks = params.getInteger("stopAt");
		RunEnvironment.getInstance().endAt(maxTicks);

		return context;
	}
}
