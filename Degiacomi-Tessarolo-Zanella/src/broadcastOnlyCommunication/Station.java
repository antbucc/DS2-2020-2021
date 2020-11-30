package broadcastOnlyCommunication;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.grid.Grid;

public class Station {
	private Grid<Object> grid;
	public final String id;
	private int ref = 0;
	protected final boolean useUnicast;
	protected final boolean useCrypto;
	protected final boolean useTopic;
	
	private Perturbation lastPerturbation; // used for logging purposes

	public Station(Grid<Object> grid, String id, boolean useUnicast, boolean useCrypto, boolean useTopic) {
		this.grid = grid;
		this.id = id;
		if (useCrypto && !useUnicast) {
			throw new IllegalArgumentException("'useCrpyto' can be true only is 'useUnicast' is true");
		}
		if (useTopic && useUnicast) {
			throw new IllegalArgumentException("'useTopic' and 'useUnicast' cannot be both true");
		}
		this.useUnicast = useUnicast;
		this.useCrypto = useCrypto;
		this.useTopic = useTopic;
		scheduleSendPerturbation();
	}
	
	private void scheduleSendPerturbation() {
		var r = new Random();
		var params = ScheduleParameters.createRepeating(r.nextInt(100), 100);
		System.out.println("Scheduled");
		
		RunEnvironment.getInstance().getCurrentSchedule().schedule(params, this, "sendPerturbation");
	}

	public void sendPerturbation() {
		var value = UUID.randomUUID().toString();
		
		// in p2p comm, all messages are unicast
		if (useUnicast) {
			Random r = new Random();

			List<Relay> relays = Utils.getAllRelaysInGrid(grid, this);
			Relay receiver = relays.remove(r.nextInt(relays.size()));

			if (useCrypto) {
				try {
					var pubKey = SimManager.keyRing.get(receiver.getRelayId()).getPublic();
					value = CryptoUtils.encryptMessage(value, pubKey);
					System.out.println("Message encrypted correctly!");
				}
				catch (Exception e) {
					System.err.println("Error encrypting the message!");
					e.printStackTrace();
				}
			}
			this.lastPerturbation = Utils.createNewUnicastPerturbation(grid, id, ref, value, this, receiver.getId());
		} else if (useTopic) {
			Random r = new Random();
			String topic = SimManager.topicsList.get(r.nextInt(SimManager.topicsList.size()));
			this.lastPerturbation = Utils.createNewTopicPerturbation(grid, id, ref, value, this, topic);
		} else {
			this.lastPerturbation = Utils.createNewPerturbation(grid, id, ref, value, this);
		}
		this.ref++;
	}

	public String getNewPerturbationValue() {
		if (this.lastPerturbation != null) {
			var val = this.lastPerturbation.val;
			this.lastPerturbation = null;

			return val;
		}
		return "";
	}

	public int getCurrentRef() {
		return this.ref;
	}

	public String getStationId() {
		return this.id;
	}
}
