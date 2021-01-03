package ssbgossip;

import analysis.EventAnalysis;
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
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.StrictBorders;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class GossipBuilder implements ContextBuilder<Object> {
	public Context<Object> context;
	public static int participants;
	private Parameters params;
	public static int bandwidth;

	@Override
	public Context<Object> build(Context<Object> context) {
		this.context = context;
		context.setId("ssbgossip");

		NetworkBuilder<Object> netBuilder1 = new NetworkBuilder<Object>("connections network", context, true);
		netBuilder1.buildNetwork();
		NetworkBuilder<Object> netBuilder2 = new NetworkBuilder<Object>("social network", context, true);
		netBuilder2.buildNetwork();
		NetworkBuilder<Object> netBuilder3 = new NetworkBuilder<Object>("block network", context, true);
		netBuilder3.buildNetwork();

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.StrictBorders(), 50, 50);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 50, 50));

		EventAnalysis ea = new EventAnalysis(context);

		params = RunEnvironment.getInstance().getParameters();
		participants = (Integer) params.getValue("participants");

		for (int i = 0; i < participants; i++) {
			try {
				context.add(new Participant(space, grid, ea));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}

		context.add(ea);
		context.add(this);

		return context;
	}

	/*
	 * Update the value of the available bandwidth in the network at each tick
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = 1000)
	public void step() {
		bandwidth = getBandwidth();
	}

	private int getBandwidth() {
		return (int) params.getValue("bandwidth");
	}
}
