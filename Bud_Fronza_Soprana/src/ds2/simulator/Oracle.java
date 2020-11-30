package ds2.simulator;

//Standard libraries
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

//Support libraries
import org.eclipse.jdt.annotation.Nullable;

//Repast libraries
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunListener;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

// Custom libraries
import ds2.utility.Event;
import ds2.utility.Logger;
import ds2.utility.Options;
import ds2.utility.RandomNormal;
import ds2.utility.Timestamped;
import ds2.visualization.DisplayManager;
import ds2.application.Id;
import ds2.application.RealApplication;
import ds2.application.events.GenerateBroadcastEvent;
import ds2.application.events.GenerateMulticastEvent;
import ds2.application.events.GenerateUnicastEvent;
import ds2.network.NetworkMessage;
import ds2.network.NetworkScheduler;
import ds2.nodes.Address;
import ds2.nodes.LocalEventScheduler;
import ds2.nodes.Machine;
import ds2.nodes.events.TriggerARQ;
import ds2.protocol.RealProtocol;
import ds2.simulator.events.CreateNode;
import ds2.simulator.events.KillNode;

public class Oracle {
	public static Oracle INSTANCE = null;
	public Logger logger = null;
	
	Context<Object> context = null;
	ArrayList<Machine> cachedMachines = new ArrayList<Machine>(); // By default empty

	double currentTimestamp = 0;

	private ArrayList<Scheduler<?>> schedulers = new ArrayList<>();
	
	public static Oracle getInstance() {
		return Oracle.INSTANCE;
	}

	public Logger getLogger() {
		return this.logger;
	}
	
	public static void mLog(Address addr, String tag, String str) {
		getInstance().getLogger().machineLog(addr, tag, Oracle.getInstance().getCurrentTimestamp(), str);
	}

	public static void mErr(Address addr, String tag, String str) {
		getInstance().getLogger().machineErr(addr, tag, Oracle.getInstance().getCurrentTimestamp(), str);
	}
	
	public static void mLog(Address addr, String tag, double timestamp, String str) {
		getInstance().getLogger().machineLog(addr, tag, timestamp, str);
	}

	public static void mErr(Address addr, String tag, double timestamp,String str) {
		getInstance().getLogger().machineErr(addr, tag, timestamp, str);
	}
	
	public void oLog(String tag, String str) {
		this.getLogger().oracleLog(tag, this.getCurrentTimestamp(), str);
	}
	
	public void oErr(String tag, String str) {
		this.getLogger().oracleErr(tag, this.getCurrentTimestamp(), str);
	}
	
	public @Nullable NetworkScheduler getNetwork() {
		return this.schedulers
				.stream()
				.filter(s -> s instanceof NetworkScheduler)
				.limit(1)
				.map(s -> (NetworkScheduler)s)
				.findAny()
				.orElse(null);
	}
	
	public @Nullable LocalEventScheduler getLocalEventsScheduler() {
		return this.schedulers
				.stream()
				.filter(s -> s instanceof LocalEventScheduler)
				.limit(1)
				.map(s -> (LocalEventScheduler)s)
				.findAny()
				.orElse(null);
	}
	
	public double getCurrentTimestamp() {
		return this.currentTimestamp;
	}

	// --- Methods to initialize
	public void init(Context<Object> context) {	
		Oracle.INSTANCE = this;
		Locale.setDefault(Locale.US); // Change locale so that default float uses dots not commas
		
		Options.addListener();
		
		this.context = context;
		
		this.schedulers.clear();
		Collections.addAll(this.schedulers, 
				new NetworkScheduler(),
				new LocalEventScheduler()
		);

		this.schedulers.add(new OnDemandScheduler<CreateNode>() {
			@Override
			public void update(Oracle oracle) {					
				int num_machines = oracle.getAllMachines().size();

				if (this.ev == null && oracle.getCurrentTimestamp() == 0 && num_machines < Options.INITIAL_NODE_SIZE) {
					this.ev = new Timestamped<>(0, new CreateNode(new Address()));
				}
				
				if (Options.MEAN_CREATE != 0 && this.ev == null && num_machines < Options.MAX_NODE_SIZE && oracle.getCurrentTimestamp() > 0) {
					double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_CREATE, Options.VAR_CREATE, 0);

					this.ev = new Timestamped<>(timestamp, new CreateNode(new Address()));
				}
			}
		});
		
		if (Options.MEAN_KILL != 0) {
			this.schedulers.add(new OnDemandScheduler<KillNode>() {
				@Override
				public void update(Oracle oracle) {
					if (this.ev == null && oracle.getCurrentTimestamp() > 0) {
						double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_KILL, Options.VAR_KILL, 0);

						Address target = Address.getRandom(); 
						
						if (target != null) {
							this.ev = new Timestamped<>(timestamp, new KillNode(target));
						}
					}
				}
			});
		}
		
		if (Options.MEAN_BROADCAST_EVENT != 0) {
			this.schedulers.add(new OnDemandScheduler<GenerateBroadcastEvent>() {
				@Override
				public void update(Oracle oracle) {
					if (this.ev == null && oracle.getCurrentTimestamp() > 0) {
						double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_BROADCAST_EVENT, Options.VAR_BROADCAST_EVENT, 0);

						Address target = Address.getRandom(); 
						
						if (target != null) {
							this.ev = new Timestamped<>(timestamp, new GenerateBroadcastEvent(target));
						}
					}				
				}
			});
		}
		
		if (Options.MEAN_MULTICAST_EVENT != 0) {
			this.schedulers.add(new OnDemandScheduler<GenerateMulticastEvent>() {
				@Override
				public void update(Oracle oracle) {
					if (this.ev == null && oracle.getCurrentTimestamp() > 0) {
						double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_MULTICAST_EVENT, Options.VAR_MULTICAST_EVENT, 0);

						Address target = Address.getRandom(); 
						
						if (target != null) {
							this.ev = new Timestamped<>(timestamp, new GenerateMulticastEvent(target));
						}
					}				
				}
			});
		}
		
		if (Options.MEAN_UNICAST_EVENT != 0) {
			this.schedulers.add(new OnDemandScheduler<GenerateUnicastEvent>() {
				@Override
				public void update(Oracle oracle) {
					if (this.ev == null && oracle.getCurrentTimestamp() > 0) {
						double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_UNICAST_EVENT, Options.VAR_UNICAST_EVENT, 0);

						Address target = Address.getRandom(); 
						
						if (target != null) {
							this.ev = new Timestamped<>(timestamp, new GenerateUnicastEvent(target));
						}
					}				
				}
			});
		}
		
		RunEnvironment.getInstance().addRunListener(new RunListener() {
			@Override
			public void stopped() {
				try {
					Logger logger = Oracle.getInstance().getLogger();
					logger.flush();
					logger.close();
				} catch (IOException e) { /* Ignore */ }
			}

			@Override
			public void paused() {
				try {
					Logger logger = Oracle.getInstance().getLogger();
					logger.flush();
				} catch (IOException e) { /* Ignore */ }
			}

			@Override
			public void started() {
				Logger.init();
				
				// Make sure address and id restart on new simulation
				Oracle.getInstance().currentTimestamp = 0; // Always starts from 0
				Id.NEXT_ID = 0;
				Address.NEXT_ADDRESS = 0;
			}

			@Override
			public void restarted() {}
		});

	}
	
	public void updateCachedMachines() {
		this.cachedMachines = new ArrayList<>();

		Context<?> context = ContextUtils.getContext(this);
		context.getObjects(Machine.class).forEach((Object o) -> this.cachedMachines.add((Machine)o));
	}
	
	public ArrayList<Machine> getAllMachines() {
		return this.cachedMachines;
	}

	public @Nullable Machine getMachine(Address addr) {
		// TODO: Recreating each time is not really efficient
		ArrayList<Machine> nodes = this.getAllMachines(); 
		
		Optional<Machine> res = nodes.stream()
			.filter((Machine m) -> m.getAddress().equals(addr))
			.limit(1)
			.findAny();
		
		return res.orElse(null);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() throws Exception {
		this.updateCachedMachines();
		
		schedulers
			.forEach((Scheduler<?> s) -> s.update(this));
		
		int indexNextTimestamp = -1;

		{
			double minTimestamp = Double.POSITIVE_INFINITY;
			for (int i=0; i<schedulers.size(); ++i) {
				Timestamped<?> t = schedulers.get(i).peek();
				if (t != null && t.getTimestamp() < minTimestamp) {
					minTimestamp = schedulers.get(i).peek().getTimestamp();
					indexNextTimestamp = i;
				}
			}
			
			if (indexNextTimestamp == -1) {
				// There is no event, we should stop 
				RunEnvironment.getInstance().endRun();
				return;
			}
		}

		Timestamped<? extends Event> tev = schedulers
				.get(indexNextTimestamp)
				.poll();
		
		if (this.currentTimestamp > tev.getTimestamp()) {
			throw new Exception("We are going back in time to " + tev.getTimestamp() + " due " + tev.getData().toString());
		}
		
		if ( Options.STOP_TIME != 0 && this.currentTimestamp > Options.STOP_TIME) {
			// Reached stop time we should stop
			RunEnvironment.getInstance().endRun();
		}
		
		this.currentTimestamp = tev.getTimestamp();
		Event ev = tev.getData();
		Address destination = ev.getDestination();

		ArrayList<Machine> machines = this.getAllMachines();
		Machine machine = machines
			.stream()
			.filter((Machine m) -> m.getAddress().equals(destination))
			.findAny()
			.orElse(null);

		if (machine == null && ev instanceof NetworkMessage) {
			((NetworkMessage)ev).setReceived(false);
		}
		
		oLog("Oracle", tev.getData().getClass().getName() + ", " + tev.getData().csv());
		
		if (ev instanceof SimulationEvent) {
			this.handleSimulatorEvent((SimulationEvent)ev);
		} else if ( machine != null && (!(ev instanceof NetworkMessage) || ((NetworkMessage)ev).isReceived())) {
			// Check if machine is present and if the event is a NetworkMessage also check that is supposed to be received
			machine.handle(ev);
		}
		
		// Graphic calls
		
		// Clear space from all the previously drawn elements
		DisplayManager.getInstance().freeSpace();
		// Pass the NetworkMessage to the graphic management
		if (ev instanceof NetworkMessage) {
			DisplayManager.getInstance().graphic_propagation((NetworkMessage)ev);
		}
	}
	
	public void handleSimulatorEvent(SimulationEvent ev) {
		if (ev instanceof CreateNode) {
			this.handleCreateNode((CreateNode)ev);
		} else if (ev instanceof KillNode) {
			this.handleKillNode((KillNode)ev);
		}
	}
	
	public void handleCreateNode(CreateNode ev) {
		Machine m = this.getMachine(ev.getDestination());
		
		if (m != null) {
			oErr("SimulationEvent", "Tried to create node twice. Call was ignored");
			return;
		}

		double ax = RandomHelper.nextDoubleFromTo(0, 1);
		double ay = RandomHelper.nextDoubleFromTo(0, 1);

		Machine machine = new Machine(ev.getDestination(), ax, ay);
		RealProtocol protocol = new RealProtocol(machine); // Maybe use factory
		RealApplication app = new RealApplication();   // Maybe use factory
		
		app.setProtocol(protocol);
		protocol.setApplication(app);
		
		machine.setProtocol(protocol);
		machine.setApplication(app);
		
		// Set next ARQ to now + smallest double increment
		this.getLocalEventsScheduler().schedule(this.getCurrentTimestamp() + Math.ulp(this.getCurrentTimestamp()), 
				                                new TriggerARQ(machine.getAddress()));
		
		this.context.add(machine);
		this.updateCachedMachines();
		DisplayManager.getInstance().moveToSpace(machine, ax * Options.DISPLAY_SIZE, ay * Options.DISPLAY_SIZE);
	}
	
	public void handleKillNode(KillNode ev) {
		Machine m = this.getMachine(ev.getDestination());
		
		if (m == null) {
			oErr("SimulationEvent", "Tried to kill a non existing or already killed node. Call was ignored");
			return;
		}
		
		this.context.remove(m);
		this.updateCachedMachines();
	}
}
