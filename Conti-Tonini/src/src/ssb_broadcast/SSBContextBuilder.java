package ssb_broadcast;

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
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import ssb_broadcast.utils.CSVHelper;

/**
 * Initializes the simulation
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class SSBContextBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("ssb_broadcast");
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		// Create CSV directory for the delay stats
		CSVHelper.createDirectory(System.currentTimeMillis());
		
		int gridSize = params.getInteger("grid_size");
		
		// Create continuous space without positioning of the agents
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new repast.simphony.space.continuous.SimpleCartesianAdder<Object>(),
				new repast.simphony.space.continuous.StickyBorders(), gridSize, gridSize);
		
		// Create grid space without positioning of the agents
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new repast.simphony.space.grid.StickyBorders(), new SimpleGridAdder<Object>(), true, gridSize, gridSize));
				
		int observersCount = params.getInteger("observers_count");
		
		Topology topology;
		
		String topologyType = params.getString("topology");
		switch(topologyType) {
			case "normal":
				topology = new NormalTopology(gridSize, observersCount);
				break;
			case "grid":
				topology = new GridTopology(gridSize, observersCount);
				break;
			case "circle":
				topology = new CircleTopology(gridSize, observersCount);
				break;
			default:
				topology = new RandomTopology(gridSize, observersCount);
				break;
		}
		
		// Position each Observer based on the topology
		for (int i = 0; i < observersCount; i++) {
			Observer observer = new Observer(i, observersCount, space, grid);
			NdPoint position = topology.getPoints().get(i);
			
			context.add(observer);
			
			space.moveTo(observer, position.getX(), position.getY());
			grid.moveTo(observer, (int)position.getX(), (int)position.getY());
		}

		return context;
	}
}
