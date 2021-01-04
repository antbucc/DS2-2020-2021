package ssb_broadcast;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.util.collections.Pair;
import ssb_broadcast.Observer.ObserverStatus;
import ssb_broadcast.utils.CryptoUtil;

/**
 * Initializes the simulation
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class SSBContextBuilder implements ContextBuilder<Object> {

	// The probability to delete an Observer
	private double deadProbability = 0;

	// The probability to create an Observer
	private double newProbability = 0;
	
	// Defines the topology of the simulation
	private Topology topology;
	
	// Space definitions
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	// Simulation's context
	private Context<Object> context;
	
	// List of active observers
	private List<Observer> observers;
	
	// Follow network
	private Network<Object> followNetwork;
	
	// Public/Private key pairs
	List<String> observersPublicKeys = new ArrayList<>();
	List<String> observersPrivateKeys = new ArrayList<>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Context build(Context<Object> context) {
		context.setId("ssb_broadcast");
		
		this.context = context;
		this.observers = new ArrayList<>();
		
		// Init params class
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		// Grid has area gridSize x gridSize
		int gridSize = params.getInteger("grid_size");
		
		deadProbability = params.getDouble("dead_probability");
		newProbability = params.getDouble("new_probability");
		
		NetworkBuilder followNetworkBuilder = new NetworkBuilder("follow", context, true);
		followNetwork = followNetworkBuilder.buildNetwork();

		NetworkBuilder blockNetworkBuilder = new NetworkBuilder("block", context, true);
		blockNetworkBuilder.buildNetwork();
		
		// Create continuous space without positioning of the agents
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context,
				new repast.simphony.space.continuous.SimpleCartesianAdder<Object>(),
				new repast.simphony.space.continuous.StickyBorders(), gridSize, gridSize);
		
		// Create grid space without positioning of the agents
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new repast.simphony.space.grid.StickyBorders(),
						new SimpleGridAdder<Object>(),
						true, gridSize, gridSize));
		
		// Number of observers to create before starting the simulation
		int observersCount = params.getInteger("observers_count");
		
		// Create public/private key pairs
		for (int i = 0; i < observersCount; i++) {
			// <public key, private key>
			Pair<String, String> keyPair = CryptoUtil.generateKeyPair();
			
			observersPublicKeys.add(keyPair.getFirst());
			observersPrivateKeys.add(keyPair.getSecond());
		}
		
		// Define topology
		String topologyType = params.getString("topology_type");
		switch (topologyType.toLowerCase()) {
			case "random":
				this.topology = new RandomTopology(gridSize);
				break;
			case "normal":
				this.topology = new NormalTopology(gridSize);
				break;
			default:
				throw new RuntimeException("unknown topology");
		}
		
		List<NdPoint> points = topology.getPoints(observersCount);

		// Position each Observer based on topology
		for (int i = 0; i < observersCount; i++) {
			Observer observer = new Observer(observersPublicKeys.get(i), observersPrivateKeys.get(i), space, grid, false);
			NdPoint position = points.get(i);
			
			context.add(observer);
			observers.add(observer);
			
			space.moveTo(observer, position.getX(), position.getY());
			grid.moveTo(observer, (int)position.getX(), (int)position.getY());
		}

		for (Observer observer: observers) {
			observer.setObservers(observers, this.followNetwork);
		}
		
		// Schedule "add or remove observers"
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters scheduleParameters = ScheduleParameters.createRepeating(0, 100);
		schedule.schedule(scheduleParameters, this, "addOrRemoveObservers");
		
		return context;
	}
	
	public void addOrRemoveObservers() {
		// Add
		if (RandomHelper.getUniform().nextDouble() > 1 - this.newProbability) {
			Pair<String, String> keyPair = CryptoUtil.generateKeyPair();
			
			Observer observer = new Observer(keyPair.getFirst(), keyPair.getSecond(), space, grid, true);

			this.context.add(observer);
			this.observers.add(observer);

			observer.setObservers(this.observers, this.followNetwork);
			NdPoint position = this.topology.getPoint();
			
			space.moveTo(observer, position.getX(), position.getY());
			grid.moveTo(observer, (int)position.getX(), (int)position.getY());
		}
		
		// Remove
		if (RandomHelper.getUniform().nextDouble() > 1 - this.deadProbability) {			
			Observer observer = this.observers.get(RandomHelper.getUniform().nextIntFromTo(0, observers.size() - 1));
			observer.setStatus(ObserverStatus.DEAD);
		}
	}
}
