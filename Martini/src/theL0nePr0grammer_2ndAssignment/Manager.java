package theL0nePr0grammer_2ndAssignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.util.SimUtilities;
import theL0nePr0grammer_2ndAssignment.Wavefronts.*;
import theL0nePr0grammer_2ndAssignment.Relays.Relay;

public class Manager implements ContextBuilder<Object>{
	private Context<Object> context;
	private ContinuousSpace<Object> space; 
	
	private int x;
	private int y;
	private int totalTick;
	
	private List<Relay> relays;
	private List<Wavefront> newestWavefronts;
	
	@Override
	public Context build(Context<Object> context) {
		context.setId("TheL0nePr0grammer_2ndAssignment");

		Parameters params = RunEnvironment.getInstance().getParameters();
		String configFile = params.getString("configFile");
		Parser config = new Parser(configFile);
		
		this.context = context;
		
		this.x = config.getDimensions().x();
		this.y = config.getDimensions().y();
		this.totalTick = config.getDuration();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new WrapAroundBorders(), this.x, this.y);
	
		this.relays = config.getRelaysList();
		this.newestWavefronts = new ArrayList<Wavefront>();
	
		
		for (Relay item : relays) {
			context.add(item);
			item.setContext(this, space);
			
			if (item.getX() == -1) {
				//random relay
				item.setX(space.getLocation(item).getX());
				item.setY(space.getLocation(item).getY());
			} else {
				space.moveTo(item, item.getX(), item.getY());
			}

			for (Relay rel : relays) {
				if (!rel.equals(item)) {			
					double distance = Math.sqrt(Math.pow(rel.getX() - item.getX(), 2) + Math.pow(rel.getY() - item.getY(), 2));
					item.addNeighbour(rel, distance);
				}
			}	
		}
		
		NetworkBuilder<Object> msgNet = new NetworkBuilder<Object>("msgNet", context, true);
		msgNet.buildNetwork();
		
		NetworkBuilder<Object> followNet = new NetworkBuilder<Object>("followNet", context, true);
		followNet.buildNetwork();
		
		NetworkBuilder<Object> blockNet = new NetworkBuilder<Object>("blockNet", context, true);
		blockNet.buildNetwork();
		
		context.add(this);
		return context;
	}
	
	public Relay getRandomRelay(Relay r) {
		Relay toRtn = r;
		while (toRtn.equals(r)) {
			SimUtilities.shuffle(relays,RandomHelper.getUniform());
			toRtn = relays.get(0);
		}
		return toRtn;
	} 
	
	public void sendWavefront(Wavefront w) {
		this.newestWavefronts.add(w);
	}

	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.LAST_PRIORITY)
	public void propagateWavefronts() {
		this.newestWavefronts.forEach((w) -> {
			if (w.getDest().getActive() && !w.getDest().equals(w.getSource())) {
				w.getDest().addWavefront(w);
			}
		});
		
		this.newestWavefronts.clear();
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void run() {
		if (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() >= totalTick) {	
			if (RunEnvironment.getInstance().getCurrentSchedule().getTickCount() == totalTick) {
				//Stop the generation of messages, to allow the system some time to get consistent
				for(Relay relay : relays) {
					relay.freeze();
				}
			}
			
			List<Relay> filtered = new ArrayList<Relay>();
			
			for (Relay r : relays) {
				if (r.getActive())
					filtered.add(r);
			}
			
			boolean result = true;
			for (Relay r : filtered) {
				for (Relay s : filtered) 
					result &= r.compareStatus(s);
			}
			
			if (result || RunEnvironment.getInstance().getCurrentSchedule().getTickCount() >= totalTick*5) {
				for (Relay r : filtered) {
					r.printStatus();
				}
				RunEnvironment.getInstance().endRun();
			}
		}
	}
	
	
}
