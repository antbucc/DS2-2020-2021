package ds2project;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.parameter.Parameters;
import repast.simphony.parameter.ParametersParser;
import repast.simphony.scenario.ScenarioUtils;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

public class test_Node {
	static Context context;
	static ContinuousSpace<Object> space;
	static Parameters params;
	static Network<Object> graph;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String scenarioDirString = "ds2project.rs";
		ScenarioUtils.setScenarioDir(new File(scenarioDirString));
		
		File paramsFile = new File(ScenarioUtils.getScenarioDir(), "parameters.xml");
		ParametersParser pp = new ParametersParser(paramsFile);
		params = pp.getParameters();
		
		RunEnvironment.init(new Schedule(), null, params, true);
		SimBuilder builder = new SimBuilder();
		
		context = new DefaultContext();
		context = builder.build(context);
		space = builder.space;
		graph = builder.graph;
		
		RunState.init().setMasterContext(context);
	}
/*
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}
*/
	@Test
	public void test_Node_init() {
		double exp_dist = params.getDouble("BroadcastDistance");
		double exp_propTime = params.getDouble("propTime");
		
		for (Object o : context.getObjects(Relay1.class)) {
			Relay1 n = (Relay1) o;
			
			assertEquals(exp_dist, n.distance, 0.01);
			assertEquals(exp_propTime, n.propTime, 0.01);
		}
	}

	@Test
	public void test_Node_next_ref() {
		for (Object o : context.getObjects(Relay1.class)) {
			Relay1 n = (Relay1) o;
			
			assertEquals(0, n.next_ref(null).intValue());
			assertEquals(
				10,
				n.next_ref(new Perturbation(n, 9, "test")).intValue()
			);
		}
	}
	
	@Test
	public void test_Node_linkNearby() {
		double max_dist = params.getDouble("BroadcastDistance");

		for (Object o1 : context.getObjects(Relay1.class)) {
			Relay1 n = (Relay1) o1;
			n.linkNearby();
			
			for (Object o2 : context.getObjects(Relay1.class)) {
				Relay1 m = (Relay1) o2;
				
				if (n.getId() == m.getId())
					continue;
				
				double dist = space.getDistance(
					space.getLocation(n),
					space.getLocation(m)
				);
				
				if (dist <= max_dist)
					assertNotNull(graph.getEdge(m, n));
				else
					assertNull(graph.getEdge(m, n));
			}
		}
	}
}
