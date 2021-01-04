package ssbGossip;

import java.io.IOException;
import java.util.Random;

import interfaces.Node;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactory;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.StrictBorders;
import repast.simphony.space.graph.Network;
import ssbGossip.Helpers.ContextHelper;
import ssbGossip.Helpers.Logger;
import ssbGossip.Helpers.ParamHelper;


public class SimBuilder implements ContextBuilder<Object> {
	Context<Object> context;
	Network<Object> graph;
	Network<Object> followGraph;
	int nNodes;

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("ssbGossip");
		ParamHelper.load();
		this.context = context;
		Parameters params = RunEnvironment.getInstance().getParameters();
		nNodes = params.getInteger("nNodes");
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context,
				new RandomCartesianAdder<>(), new StrictBorders(), 50, 50);

		NetworkFactory netFact = NetworkFactoryFinder.createNetworkFactory(null);
		graph = netFact.createNetwork("graph", context, false, new CustomEdgeCreator());
		followGraph = netFact.createNetwork("followGraph", context, true);
		ContextHelper.context = context;
		ContextHelper.graph = graph;
		ContextHelper.space = space;
		ContextHelper.followGraph = followGraph;
		ISchedule s = RunEnvironment.getInstance().getCurrentSchedule();
		s.schedule(ScheduleParameters.createRepeating(s.getTickCount()+ParamHelper.insertInterval, 
				ParamHelper.insertInterval), this,"randomInsertion");
		s.schedule(ScheduleParameters.createRepeating(s.getTickCount()+ParamHelper.failInterval, 
				ParamHelper.failInterval), this,"randomFailure");
		s.schedule(ScheduleParameters.createAtEnd(ScheduleParameters.LAST_PRIORITY), this, "closeLog");
		try {
			Logger.load();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		for (int i = 0; i < nNodes; i++) {
			if (ParamHelper.simType.equals("OpenGossip"))
				context.add(new OpenGossip(false));
			else
				context.add(new TransitiveInterest(false));
		}

		return context;

	}
	
	public void randomInsertion() {
		Random r = new Random();
		if(r.nextFloat()<ParamHelper.insertProb) {
			if (ParamHelper.simType.equals("OpenGossip"))
				context.add(new OpenGossip(true));
			else
				context.add(new TransitiveInterest(true));
		}
	}
	
	public void randomFailure() {
		Random r = new Random();
		if(r.nextFloat()<ParamHelper.failureProb) {
			Node n = (Node) context.getRandomObjects(Node.class, 1).iterator().next();
			n.fail();
		}
	}
	
	public void closeLog() {
		try {
			Logger.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
