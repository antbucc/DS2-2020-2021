package ds2project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactory;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.StrictBorders;
import repast.simphony.space.graph.Network;
import repast.simphony.util.collections.IndexedIterable;


public class SimBuilder implements ContextBuilder<Object> {
	int nNodes;
	ContinuousSpace<Object> space;
	Network<Object> graph;
	float insertProb, insertInterval;
	float failProb, failInterval;
	Context<Object> context;
	int lastI;
	int sim_type;

	@Override
	public Context build(Context<Object> context) {
		
		Path path_to_delete = Paths.get("output", "Repast_output.csv");

	    if (Files.exists(path_to_delete)) try {
	        Files.delete(path_to_delete);
	    } catch (IOException e) {

	        e.printStackTrace();
	    }
		
		context.setId("ds2project");
		this.context = context;
		RunEnvironment.getInstance().endAt(400000);
		// Retrieval of the simulation parameters
		Parameters params = RunEnvironment.getInstance().getParameters();
		nNodes = params.getInteger("nNodes");
		insertInterval = params.getFloat("insertInterval");
		insertProb = params.getFloat("insertProb");
		failProb = params.getFloat("failProb");
		failInterval = params.getFloat("failInterval");
		sim_type = params.getInteger("sim_type");

		// Creation of the continuous space
		ContinuousSpaceFactory factory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = factory.createContinuousSpace(
			"space",
			context,
			new RandomCartesianAdder<>(),
			new StrictBorders(),
			50,
			50
		);

		NetworkFactory netFact = NetworkFactoryFinder.createNetworkFactory(null);
		graph = netFact.createNetwork("graph", context, false, new CustomEdgeCreator());

		lastI = 0;
		for (int i = 0; i < nNodes; i++) {
			context.add(createNode());
		}

		if (sim_type > 1) {
			// Schedule insertions
			RunEnvironment.getInstance().getCurrentSchedule()
				.schedule(
					ScheduleParameters.createRepeating(
						RunEnvironment.getInstance().getCurrentSchedule().getTickCount() + insertInterval,
						insertInterval
					),
					this,
					"randomInsertion"
				);

			// Schedule failures
			RunEnvironment.getInstance().getCurrentSchedule()
				.schedule(
					ScheduleParameters.createRepeating(
						RunEnvironment.getInstance().getCurrentSchedule().getTickCount() + failInterval,
						failInterval
					),
					this,
					"randomFailure"
				);
		}

		return context;
	}

	public void randomInsertion() {
		Random r = new Random();
		if (r.nextFloat() <= insertProb) {
			Relay1 n = createNode();
			context.add(n);
			n.linkNearby();
		}
	}

	public void randomFailure() {
		Random r = new Random();

		IndexedIterable<Object> nodes = context.getObjects(Relay1.class);
		int idx = r.nextInt(nodes.size());
		Relay1 n = (Relay1) nodes.get(idx);

		if (r.nextFloat() <= failProb)
			n.fail();
	}

	private Relay1 createNode() {
		Relay1 n;
		switch (sim_type) {
			case 1:
				n = new Relay1(space, lastI, graph);
				break;
			case 2:
				n = new Relay2(space, lastI, graph);
				break;
			case 3:
				n = new Relay3(space, lastI, graph);
				break;
			case 4:
				n = new Relay3Unicast(space, lastI, graph);
				break;
			case 5:
				n = new Relay3UnicastEncryption(space, lastI, graph);
				break;
			case 6:
				n = new Relay3Multicast(space, lastI, graph);
				break;
			default:
				n = new Relay1(space, lastI, graph);
		}
		lastI++;
		return n;
	}
}
