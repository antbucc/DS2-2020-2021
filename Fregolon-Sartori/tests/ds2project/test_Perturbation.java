package ds2project;

import static org.junit.Assert.*;

import java.io.File;
import java.util.LinkedList;

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

public class test_Perturbation {
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
		params.setValue("sim_type", 3);
		params.setValue("nNodes", 50);
		
		RunEnvironment.init(new Schedule(), null, params, true);
		SimBuilder builder = new SimBuilder();
		
		context = new DefaultContext();
		context = builder.build(context);
		space = builder.space;
		graph = builder.graph;
		
		RunState.init().setMasterContext(context);
	}
	
	@Before
	public void setUp() throws Exception {
		for (Object o1 : context.getObjects(Relay3.class)) {
			Relay3 n = (Relay3) o1;
			n.linkNearby();
		}
	}
		
	@Test
	public void testPerturbationInit() {
		Relay3 src = (Relay3)context.getObjects(Relay3.class).get(0);
		Integer ref = 42;
		String val = "Test message";
		
		Perturbation p = new Perturbation(src, ref, val);
		
		assertEquals(src, p.getSrc());
		assertEquals(ref, p.getRef());
		assertEquals(val, p.getVal());
	}

	@Test
	public void testFrontier() {
		Relay3 src = (Relay3)context.getObjects(Relay3.class).get(0);
		Integer ref = 42;
		String val = "Test message";
		Perturbation p1 = new Perturbation(src, ref, val + " 1"),
					 p2 = new Perturbation(src, ref+1, val + " 2");
		
		src.forward(p1);
		src.forward(p2);
		
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		for (int i = 0; i < 10000; i++)
		    schedule.execute();
		
		for (Object o : context.getObjects(Relay1.class)) {
			Relay3 n = (Relay3) o;
			LinkedList<Perturbation> log = n.log.get(src);
			assertNotNull(log);
			assertEquals(2, log.size());
			assertEquals(ref+0, log.get(0).getRef().intValue());
			assertEquals(val + " 1", log.get(0).getVal());
			assertEquals(ref+1, log.get(1).getRef().intValue());
			assertEquals(val + " 2", log.get(1).getVal());
		}
	}
}
