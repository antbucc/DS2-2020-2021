package bcastonly;

import analysis.PerturbationAnalysis;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.collections.IndexedIterable;

public class BcastOnlyBuilder implements ContextBuilder<Object> {

	public Context<Object> context;
	public static int MAX_RELAY;
	public static int relay_count;
	private Parameters params;
	public static int bandwidth;

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("bcastonly");
		this.context = context;

		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>(
				"p2p network", context, true);
		netBuilder.buildNetwork();
		NetworkBuilder<Object> net2Builder = new NetworkBuilder<Object>(
				"p2m network", context, true);
		net2Builder.buildNetwork();
		NetworkBuilder<Object> net3Builder = new NetworkBuilder<Object>(
				"p2s network", context, true);
		net3Builder.buildNetwork();

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 50,
				50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, 50, 50));

		PerturbationAnalysis pa = new PerturbationAnalysis(context);

		params = RunEnvironment.getInstance().getParameters();
		relay_count = (Integer) params.getValue("relay_count");
		MAX_RELAY = relay_count * 10;
		for (int i = 0; i < relay_count; i++) {
			context.add(new Relay(space, grid, pa, RandomHelper.nextIntFromTo(
					-10, 0)));
		}
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}

		context.add(pa);
		context.add(this);

		RunEnvironment.getInstance().setScheduleTickDelay(20);

		return context;
	}

	/*
	 * Update the value of the available bandwidth in the network at each tick
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 1000)
	public void step() {
		bandwidth = getBandwidth();
	}

	/*
	 * Add and remove nodes int the network 
	 */
	@ScheduledMethod(start = 10, interval = 10)
	public void dinamicNetwork() {
		int remove = RandomHelper.nextIntFromTo(0, getDecreaseFactor());
		while (remove > 0 && relay_count > 2) {
			IndexedIterable<Object> relays = context.getObjects(Relay.class);
			Relay r = (Relay) relays.get(RandomHelper.nextIntFromTo(0,
					relay_count - 1));
			context.remove(r);
			relay_count--;
			remove--;
		}
		int add = RandomHelper.nextIntFromTo(0, getIncreaseFactor());
		while (add > 0 && relay_count < MAX_RELAY) {
			ContinuousSpace<Object> space = (ContinuousSpace<Object>) context
					.getProjection("space");
			Grid<Object> grid = (Grid<Object>) context.getProjection("grid");
			IndexedIterable<Object> pa = context
					.getObjects(PerturbationAnalysis.class);
			Relay r = new Relay(space, grid, (PerturbationAnalysis) pa.get(0),
					RandomHelper.nextIntFromTo(-10, 0));
			context.add(r);
			relay_count++;
			NdPoint pt = space.getLocation(r);
			grid.moveTo(r, (int) pt.getX(), (int) pt.getY());
			add--;
		}
	}

	private int getIncreaseFactor() {
		return (int) params.getValue("increase_factor");
	}

	private int getDecreaseFactor() {
		return (int) params.getValue("decrease_factor");
	}

	private int getBandwidth() {
		return (int) params.getValue("bandwidth");
	}

}
