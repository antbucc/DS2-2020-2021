package ds2.simulator;

//Standard libraries
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

//Apache commons
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

//Support libraries
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.intellij.lang.annotations.PrintFormat;

//Repast libraries
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunListener;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

// Custom libraries
import ds2.utility.Event;
import ds2.utility.EventHandler;
import ds2.utility.Options;
import ds2.utility.RandomNormal;
import ds2.utility.Timestamped;
import ds2.utility.Options.ProtocolType;
import ds2.utility.logging.Logger;
import ds2.visualization.DisplayManager;
import ds2.application.ApplicationEvent;
import ds2.application.FollowEventScheduler;
import ds2.application.Id;
import ds2.application.RealApplication;
import ds2.application.events.BlockEvent;
import ds2.application.events.FollowEvent;
import ds2.application.events.GenerateEvent;
import ds2.application.events.UnBlockEvent;
import ds2.application.events.UnFollowEvent;
import ds2.network.NetworkMessage;
import ds2.network.NetworkScheduler;
import ds2.nodes.Address;
import ds2.nodes.LocalEvent;
import ds2.nodes.LocalEventScheduler;
import ds2.nodes.Machine;
import ds2.nodes.events.TriggerUpdate;
import ds2.protocol.Protocol;
import ds2.protocol.SSBProtocol;
import ds2.protocol.SSBTransitiveInterest;
import ds2.protocol.ssb.Identity;
import ds2.protocol.ssb.ProtocolMessage;
import ds2.simulator.events.CreateNode;
import ds2.simulator.events.GoOffline;
import ds2.simulator.events.KillNode;

/**
 * This class represents the coordinator of the entire simulation. It will at each step decide the event which will be handled next and it will dispatch it to the correct class for its handling
 */
public class Oracle extends EventHandler {
	public static Oracle INSTANCE = null;
	public Logger logger = null;
	
	private Context<Object> context = null;
	private ArrayList<Machine<?, ?>> cachedMachines = new ArrayList<>(); // By default empty
	private ArrayList<Pair<Address, PublicKey>> cachedAddressesAndPorts = new ArrayList<>();

	private double currentTimestamp = 0;

	private ArrayList<Scheduler<?>> schedulers = new ArrayList<>();
	
	/**
	 * Obtain an instance of oracle (singleton)
	 * @return an instance of oracle
	 */
	public static Oracle getInstance() {
		return Oracle.INSTANCE;
	}

	/**
	 * Returns an instance to the Logger utilty
	 * @return A instance of the logger utility if Oracle was already initialized, null otherwise
	 */
	public Logger getLogger() {
		return this.logger;
	}
	
	/**
	 * Log something in the oracle log using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, @NonNull String msg) {
		this.log(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}
	
	/**
	 * Log something in the oracle log using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void log(@NonNull String tag, double timestamp, @NonNull String msg) {
		getLogger().getOracleLogger().print(System.out, msg, tag, ""+timestamp);
	}

	/**
	 * Log an error in the oracle log using the current timestamp
	 * @param tag The tag to use
	 * @param msg The message to log
	 */
	public void err(@NonNull String tag, @NonNull String msg) {
		this.err(tag, Oracle.getInstance().getCurrentTimestamp(), msg);
	}

	/**
	 * Log an error in the oracle log using a custom timestamp
	 * @param tag The tag to use
	 * @param timestamp The timestamp to use
	 * @param msg The message to log
	 */
	public void err(@NonNull String tag, double timestamp, @NonNull String msg) {
		getLogger().getOracleLogger().print(System.err, msg, tag, ""+timestamp);
	}
		
	/**
	 * Returns the FollowEventScheduler of this simulation
	 * @return the instance of the FollowEventScheduler used in this simulation if there is one, null otherwise
	 */
	public @Nullable FollowEventScheduler getFollowEventScheduler() {
		return this.schedulers
				.stream()
				.filter(s -> s instanceof FollowEventScheduler)
				.limit(1)
				.map(s -> (FollowEventScheduler)s)
				.findAny()
				.orElse(null);
	}
	
	/**
	 * Returns the NetworkScheduler of this simulation
	 * @return the instance of the NetworkScheduler used in this simulation if there is one, null otherwise
	 */
	public @Nullable NetworkScheduler getNetwork() {
		return this.schedulers
				.stream()
				.filter(s -> s instanceof NetworkScheduler)
				.limit(1)
				.map(s -> (NetworkScheduler)s)
				.findAny()
				.orElse(null);
	}
	
	/**
	 * Returns the LocalEventScheduler of this simulation
	 * @return the instance of the LocalEventScheduler used in this simulation if there is one, null otherwise
	 */
	public @Nullable LocalEventScheduler getLocalEventsScheduler() {
		return this.schedulers
				.stream()
				.filter(s -> s instanceof LocalEventScheduler)
				.limit(1)
				.map(s -> (LocalEventScheduler)s)
				.findAny()
				.orElse(null);
	}
	
	/**
	 * Returns the RecreateNodeScheduler of this simulation
	 * @return the instance of the RecreateNodeScheduler used in this simulation if there is one, null otherwise
	 */
	public @Nullable RecreateNodeScheduler getRecreateNodeScheduler() {
		return this.schedulers
				.stream()
				.filter(s -> s instanceof RecreateNodeScheduler)
				.limit(1)
				.map(s -> (RecreateNodeScheduler)s)
				.findAny()
				.orElse(null);
	}
	
	/**
	 * Returns the current timestamp of the simulation
	 * @return the current timestamp of the simulation
	 */
	public double getCurrentTimestamp() {
		return this.currentTimestamp;
	}
	
	/**
	 * Obtain a random pair of address and port (of an alive machine)
	 * @return A pair of address and port
	 */
	public Pair<Address, PublicKey> getRandomAddressAndPort() {
		return Oracle.getInstance().getAllAddressesAndPorts().get(RandomHelper.nextIntFromTo(0, Oracle.getInstance().getAllAddressesAndPorts().size()-1));
	}

	/**
	 * Initializes the context and all the various utilities used in the simulation given the context on which the simulation runs
	 * @param context The context of the simulation
	 */
	public void init(Context<Object> context) {	
		Oracle.INSTANCE = this;
		Locale.setDefault(Locale.US); // Change locale so that default float uses dots not commas
		
		Options.addListener();
		
		this.context = context;
		
		this.schedulers.clear();
		Collections.addAll(this.schedulers, 
				new NetworkScheduler(),
				new LocalEventScheduler(),
				new RecreateNodeScheduler()
		);
		
		if (Options.PROTOCOL_TYPE.equals(ProtocolType.TRANSITIVE_INTEREST)) {
			this.schedulers.add(new FollowEventScheduler());
		}

		this.schedulers.add(new OnDemandScheduler<CreateNode>() {
			public CreateNode generateCreate() {

				double ax = RandomHelper.nextDoubleFromTo(0, 1);
				double ay = RandomHelper.nextDoubleFromTo(0, 1);
				
				Machine<Protocol<?,?>, ?> machine = new Machine<>(new Address(), ax, ay);
				
				// We get the random number then we add 1 to transpose the distribution of 1 
				int numIdentities = RandomHelper.createGamma(Options.ALPHA_IDENTITIES_PER_NODE, Options.LAMBDA_IDENTITIES_PER_NODE).nextInt() + 1; 

				KeyPairGenerator kpg;
				
				try {
					kpg = KeyPairGenerator.getInstance("RSA");
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					System.exit(1); // Doesn't really makes sense continuing otherwise
					return null;
				}

				kpg.initialize(1024); // key size of 1024bit
				
				ArrayList<Identity> identities = new ArrayList<Identity>() {{
					for (int i=0; i<numIdentities; ++i) {
						KeyPair kp = kpg.genKeyPair();
						add(new Identity(kp.getPublic(), kp.getPrivate()));
					}					
				}}; 
				
				ArrayList<Protocol> protocols = new ArrayList<Protocol>() {{
					for (int i=0; i<identities.size(); i++) {
						Protocol<RealApplication,?> protocol = null;
						switch (Options.PROTOCOL_TYPE) {
							case OPEN_GOSSIP: protocol = new SSBProtocol((Machine<SSBProtocol, ?>)((Machine)machine), identities.get(i)); break;
							case TRANSITIVE_INTEREST: protocol = new SSBTransitiveInterest((Machine<SSBTransitiveInterest, ?>)((Machine)machine), identities.get(i)); break;
						}

						RealApplication app = new RealApplication(protocol);   // Maybe use factory
						protocol.addUp(app);
//						System.err.println("Length " + protocol.getUps().size());
						add(protocol);
					}
				}};
				

				
				
//				return new CreateNode(new Address(), identities);
				
				return new CreateNode(machine.getAddress(), protocols, machine);
			}
			
			@Override
			public void update(Oracle oracle) {					
				int num_machines = oracle.getAllMachines().size();

				if (this.ev == null && oracle.getCurrentTimestamp() == 0 && num_machines < Options.INITIAL_NODE_SIZE) {
					this.ev = new Timestamped<>(0, generateCreate());
				}
				
				if (Options.MEAN_CREATE != 0 && this.ev == null && num_machines < Options.MAX_NODE_SIZE && oracle.getCurrentTimestamp() > 0) {
					double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_CREATE, Options.VAR_CREATE, 0);

					this.ev = new Timestamped<>(timestamp, generateCreate());
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
		
		if (Options.MEAN_GOOFFLINE != 0) {
			this.schedulers.add(new OnDemandScheduler<GoOffline>() {
				@Override
				public void update(Oracle oracle) {
					if (this.ev == null && oracle.getCurrentTimestamp() > 0) {
						double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_GOOFFLINE, Options.VAR_GOOFFLINE, 0);

						Address target = Address.getRandom(); 

						if (target != null) {
							this.ev = new Timestamped<>(timestamp, new GoOffline(target));
						}
					}
				}
			});
		}
		
		if (Options.MEAN_EVENT != 0) {
			this.schedulers.add(new OnDemandScheduler<GenerateEvent>() {
				@Override
				public void update(Oracle oracle) {
					if (Oracle.getInstance().getAllAddressesAndPorts().isEmpty())
						return;
					
					if (this.ev == null && oracle.getCurrentTimestamp() >= 0) {
						double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_EVENT, Options.VAR_EVENT, 0);
						
						Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
						
						this.ev = new Timestamped<>(timestamp, new GenerateEvent(addressAndPort.getLeft(), addressAndPort.getRight()));
					}				
				}
			});
		}
		
		// Transitive interest only events
		if (Options.PROTOCOL_TYPE.equals(ProtocolType.TRANSITIVE_INTEREST)) {
			if (Options.MEAN_FOLLOW != 0) {
				this.schedulers.add(new OnDemandScheduler<FollowEvent>() {
					@Override
					public void update(Oracle oracle) {
						if (Oracle.getInstance().getAllAddressesAndPorts().isEmpty())
							return;
						
						if (this.ev == null && oracle.getCurrentTimestamp() >= 0) {
							double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_FOLLOW, Options.VAR_FOLLOW, 0);
							
							Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
							
							this.ev = new Timestamped<>(timestamp, new FollowEvent(addressAndPort.getLeft(), addressAndPort.getRight()));
						}				
					}
				});
			}

			if (Options.MEAN_UNFOLLOW != 0) {
				this.schedulers.add(new OnDemandScheduler<UnFollowEvent>() {
					@Override
					public void update(Oracle oracle) {
						if (Oracle.getInstance().getAllAddressesAndPorts().isEmpty())
							return;
						
						if (this.ev == null && oracle.getCurrentTimestamp() >= 0) {
							double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_UNFOLLOW, Options.VAR_UNFOLLOW, 0);
							
							Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
							
							this.ev = new Timestamped<>(timestamp, new UnFollowEvent(addressAndPort.getLeft(), addressAndPort.getRight()));
						}				
					}
				});
			}

			if (Options.MEAN_BLOCK != 0) {
				this.schedulers.add(new OnDemandScheduler<BlockEvent>() {
					@Override
					public void update(Oracle oracle) {
						if (Oracle.getInstance().getAllAddressesAndPorts().isEmpty())
							return;
						
						if (this.ev == null && oracle.getCurrentTimestamp() >= 0) {
							double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_BLOCK , Options.VAR_BLOCK, 0);
							
							Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
							
							this.ev = new Timestamped<>(timestamp, new BlockEvent(addressAndPort.getLeft(), addressAndPort.getRight()));
						}				
					}
				});
			}
			
			if (Options.MEAN_UNBLOCK != 0) {
				this.schedulers.add(new OnDemandScheduler<UnBlockEvent>() {
					@Override
					public void update(Oracle oracle) {
						if (Oracle.getInstance().getAllAddressesAndPorts().isEmpty())
							return;
						
						if (this.ev == null && oracle.getCurrentTimestamp() >= 0) {
							double timestamp = oracle.getCurrentTimestamp() + RandomNormal.random(Options.MEAN_UNBLOCK , Options.VAR_UNBLOCK, 0);
							
							Pair<Address, PublicKey> addressAndPort = Oracle.getInstance().getRandomAddressAndPort();
							
							this.ev = new Timestamped<>(timestamp, new UnBlockEvent(addressAndPort.getLeft(), addressAndPort.getRight()));
						}				
					}
				});
			}
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
	
	/**
	 * Updates the cached list of machines
	 */
	public void updateCachedMachines() {
		this.cachedMachines = new ArrayList<>();

		Context<?> context = ContextUtils.getContext(this);
		context.getObjects(Machine.class).forEach((Object o) -> this.cachedMachines.add((Machine<?, ?>)o));
	}

	/**
	 * Updates the cached list of addresses and ports
	 */
	public void updateCachedAddressesAndPorts() {
		this.cachedAddressesAndPorts = new ArrayList<>();

		this.getAllMachines().forEach((m) -> {
			m.getUps()
			 .stream()
			 .map( (p) -> p.getPort())
			 .forEach( (p) -> this.cachedAddressesAndPorts.add(new MutablePair<>(m.getAddress(), p)));;
		});
	}
	
	/**
	 * Obtains all (alive) machines
	 * @return the list of (alive) machines
	 */
	public ArrayList<Machine<?, ?>> getAllMachines() {
		return this.cachedMachines;
	}

	/**
	 * Obtains all the address and port pairs
	 * @return the list of all address and port pairs
	 */
	public ArrayList<Pair<Address, PublicKey>> getAllAddressesAndPorts() {
		return this.cachedAddressesAndPorts;
	}
	
	/**
	 * Obtains a machine given its address
	 * @param addr The address of the machine
	 * @return the machine if the machine exists and is alive, null otherwise
	 */
	public @Nullable Machine<?, ?> getMachine(Address addr) {
		ArrayList<Machine<?, ?>> nodes = this.getAllMachines(); 
		
		Optional<Machine<?, ?>> res = nodes.stream()
			.filter((m) -> m.getAddress().equals(addr))
			.limit(1)
			.findAny();
		
		return res.orElse(null);
	}
	
	/**
	 * This function executes a single step of the simulation
	 * @throws Exception Allow for sub-functions to throw if something happens
	 */
	@ScheduledMethod(start = 1, interval = 1)
	public void step() throws Exception {
		this.updateCachedMachines();
		this.updateCachedAddressesAndPorts();
		
		schedulers
			.forEach((Scheduler<?> s) -> s.update(this));

		Optional<Scheduler<?>> minScheduler = schedulers
				.stream()
				.filter((s) -> s.peek() != null)
				.min(Comparator.comparing((s) -> s.peek().getTimestamp()));
		
		if (!minScheduler.isPresent()) {
			// There is no scheduler with a non null event, we should stop 
			RunEnvironment.getInstance().endRun();
			return;
		}
		
		Timestamped<? extends Event> tev = minScheduler.get().poll();
		
		if (this.currentTimestamp > tev.getTimestamp()) {
			throw new Exception("We are going back in time to " + tev.getTimestamp() + " due " + tev.getData().toString());
		}
		
		if ( Options.STOP_TIME != 0 && this.currentTimestamp > Options.STOP_TIME) {
			// Reached stop time we should stop
			RunEnvironment.getInstance().endRun();
		}
		
		this.currentTimestamp = tev.getTimestamp();
		Event ev = tev.getData();
				
		log("Oracle", tev.getData().getClass().getName() + " " + tev.getData());
		
		// Clear space from all the previously drawn elements
		DisplayManager.getInstance().freeSpace();

		// Then call handle
		this.handle(ev);
	}
	
	/**
	 * This function is used to handle and more specifically to dispatch any networkMessage, localEvent or applicationEvent
	 * @param ev The event to dispatch
	 */
	@EventHandler.NetworkMessageHandler(dataCls = Object.class)
	@EventHandler.LocalEventHandler(cls = LocalEvent.class)
	@EventHandler.ApplicationEventHandler(cls = ApplicationEvent.class)
	public void handleMachineEvents(Event ev) {
		Address destination = ev.getDestination();

		ArrayList<Machine<?, ?>> machines = this.getAllMachines();
		Machine<?, ?> machine = machines
			.stream()
			.filter((m) -> m.getAddress().equals(destination))
			.findAny()
			.orElse(null);

		if ( machine != null && (!(ev instanceof NetworkMessage) || ((NetworkMessage<?>)ev).isReceived())) {
			// Check if machine is present and if the event is a NetworkMessage also check that is supposed to be received
			machine.handle(ev);
		}
		
		// Graphic calls
		
		// Pass the NetworkMessage to the graphic management
		if (ev instanceof NetworkMessage) {
			DisplayManager.getInstance().graphic_propagation((NetworkMessage<ProtocolMessage<?>>)ev);
		}
	}
	
	/**
	 * This function is used to handle a CreateNode event
	 * @param ev The event to handle
	 */
	@EventHandler.SimulationEventHandler(cls = CreateNode.class)
	public void handleCreateNode(CreateNode ev) {
		
//		if (this.getMachine(ev.getDestination()) != null) {
//			err("SimulationEvent", "Tried to create node twice. Call was ignored");
//			return;
//		}
//
//		double ax = RandomHelper.nextDoubleFromTo(0, 1);
//		double ay = RandomHelper.nextDoubleFromTo(0, 1);
//		
//		Machine<Protocol<?,?>, ?> machine = new Machine<>(ev.getDestination(), ax, ay);

//		ev.getIdentities().forEach((kp) -> {
//			Protocol<RealApplication,?> protocol = null;
//			switch (Options.PROTOCOL_TYPE) {
//				case OPEN_GOSSIP: protocol = new SSBProtocol((Machine<SSBProtocol, ?>)((Machine)machine), kp); break;
//				case TRANSITIVE_INTEREST: protocol = new SSBTransitiveInterest((Machine<SSBTransitiveInterest, ?>)((Machine)machine), kp); break;
//			}
//			machine.addUp(protocol);
//			
//			RealApplication app = new RealApplication(protocol);   // Maybe use factory
//			protocol.addUp(app);				
//
//			// Set next events to now + smallest double increment
//			double staggerTime = this.getCurrentTimestamp() == 0? RandomHelper.createUniform(0, Options.UPDATE_INTERVAL).nextDouble() : 0;
//			
//			if (Options.PROTOCOL_TYPE.equals(ProtocolType.TRANSITIVE_INTEREST)) {
//				int randomNoFollowed = RandomHelper.nextIntFromTo(15, 50);
//				for (int i = 0; i < randomNoFollowed; i++ ) {
//					FollowEvent event = new FollowEvent(machine.getAddress(), protocol.getPort());
////					this.handle(event);
////					this.getLocalEventsScheduler().schedule(this.getCurrentTimestamp() + Math.ulp(this.getCurrentTimestamp()) ,
////							new FollowEvent(machine.getAddress(), protocol.getPort()));
//					
//				}
//			}			
//			this.getLocalEventsScheduler().schedule(this.getCurrentTimestamp() + staggerTime + Math.ulp(this.getCurrentTimestamp()), 
//					                                new TriggerUpdate(machine.getAddress(), protocol.getPort()));
//			
//			
//		});
		Machine<Protocol<?,?>, ?> machine = ev.getMachine();
		ev.getProtocols().forEach((protocol) -> {

			machine.addUp(protocol);
			protocol.setDown(machine);			
			
			if ((protocol instanceof SSBTransitiveInterest) && ((SSBTransitiveInterest)protocol).getFollowed().isEmpty()) {
				int numInstances = RandomHelper.createUniform(Options.MIN_FOLLOWED_PER_NODE, Options.MAX_FOLLOWED_PER_NODE).nextInt();
				for (int i=0; i < numInstances; ++i) {
					this.getFollowEventScheduler().schedule(this.getCurrentTimestamp() + Math.ulp(this.getCurrentTimestamp()), 
                                                            new FollowEvent(machine.getAddress(), protocol.getPort()));					
				}
			}
			
			// Set next events to now + smallest double increment
			double staggerTime = this.getCurrentTimestamp() == 0? RandomHelper.createUniform(0, Options.UPDATE_INTERVAL).nextDouble() : 0;
			
			this.getLocalEventsScheduler().schedule(this.getCurrentTimestamp() + staggerTime + 2*Math.ulp(this.getCurrentTimestamp()), 
					                                new TriggerUpdate(machine.getAddress(), protocol.getPort()));
			
	
		});
		
		this.context.add(machine);
		
		this.updateCachedMachines();
		this.updateCachedAddressesAndPorts();
		
		DisplayManager.getInstance().moveToSpace(machine, machine.getPosX() * Options.DISPLAY_SIZE, machine.getPosY() * Options.DISPLAY_SIZE);
	}
	
	/**
	 * This functions is used to handle a KillNode event
	 * @param ev The event to handle
	 */
	@EventHandler.SimulationEventHandler(cls = KillNode.class)
	public void handleKillNode(KillNode ev) {
		Machine<?, ?> m = this.getMachine(ev.getDestination());
		
		if (m == null) {
			err("SimulationEvent", "Tried to kill a non existing or already killed node. Call was ignored");
			return;
		}
		
		this.context.remove(m);
		this.updateCachedMachines();
		this.updateCachedAddressesAndPorts();
	}
	
	/**
	 * This functions is used to handle a GoOffline event
	 * @param ev The event to handle
	 */
	@EventHandler.SimulationEventHandler(cls = GoOffline.class)
	public void handleGoOffline(GoOffline ev) {

		ArrayList<Protocol> protocols = new ArrayList<Protocol>(
			this.getMachine(ev.getDestination()).getUps().stream().map( (p) -> {
				if (p instanceof SSBProtocol) {
					return ((SSBProtocol)p);
				} else if (p instanceof SSBTransitiveInterest) {
					return ((SSBTransitiveInterest)p);
				} else {
					System.err.printf("Impossible to get the identity of the protocol");
					System.exit(1);
					return null;
				}
			} ).collect(Collectors.toList()));
		
		this.handleKillNode(new KillNode(ev.getDestination()));
		
		double delay = RandomNormal.random(Options.MEAN_RECREATE_DELAY, Options.VAR_RECREATE_DELAY, 0);

		// Bisognerebbe prendere anche questi dati dalla macchina vecchia, se no la nuova avr� un'altra posizione
		double ax = RandomHelper.nextDoubleFromTo(0, 1);
		double ay = RandomHelper.nextDoubleFromTo(0, 1);
		
		Machine<Protocol<?,?>, ?> online = new Machine(ev.getDestination(), ax, ay);
		this.getRecreateNodeScheduler().schedule(this.getCurrentTimestamp() + delay, new CreateNode(ev.getDestination(), protocols, online));
	}
	
	/**
	 * Auxiliary function to obtain a machine from a PublicKey
	 * @param src
	 * @return the machine where the public key was used
	 */
	public Machine<?,?> fromKeyToMachine(PublicKey src) {
		ArrayList<Pair<Address, PublicKey>> graphicSet = this.getAllAddressesAndPorts();
		Iterator<Pair<Address, PublicKey>> iter = graphicSet.iterator();
		Address target = null;
		while (iter.hasNext()) {
			Pair<Address, PublicKey> element = iter.next(); 
			if (element.getRight().equals(src)) {
				target = element.getLeft();
			}
		}
		return this.getMachine(target);
	}
}
