package ds2.protocol;

// Standard libraries
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

//Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.application.ApplicationData;
import ds2.application.RealApplication;
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.nodes.events.TriggerARQ;
import ds2.protocol.messages.ARQHistoryReply;
import ds2.protocol.messages.ARQSoliton;
import ds2.protocol.messages.ProtocolMsg;
import ds2.protocol.messages.Soliton;
import ds2.protocol.messages.data.BroadcastMsg;
import ds2.protocol.messages.data.MulticastMsg;
import ds2.protocol.messages.data.UnicastMsg;
import ds2.simulator.Oracle;
import ds2.utility.Options;

//Repast libraries
import repast.simphony.random.RandomHelper;

public class RealProtocol extends Protocol<RealApplication> {
	
	/**
	 * The sequence number for own messages.
	 */
	private int mySeqn = 0;
	
	/**
	 * The complete log of per-source solitons.
	 */
	private HashMap<Address, ArrayList<Soliton>> log;
	
	/**
	 * Agent to handle peer-to-peer communication.
	 */
	private UnicastProtocol p2p;
	
	/**
	 * Agent to handle multicast communication.
	 */
	private MulticastProtocol multicast; 
	
	/** 
	 * Requests received, for analysis purposes (only one request per source + seqn can be present)
	 */
	private ArrayList<ARQSoliton> toServe;

	/**
	 * Variable to check whether a received message is actually new for the node
	 */
	private boolean receivedNew;
	
	/**
	 * Constructor.
	 * @param machine the machine on which the protocol is running.
	 */
	public RealProtocol(@NonNull Machine machine) {
		super(machine);
		this.log = new HashMap<>();
		toServe = new ArrayList<>();
		
		p2p = new UnicastProtocol(this);
		ArrayList<Integer> topics = new ArrayList<>();
		
		// Assign a random number (up to 5) of random topics to the multicast protocol (between 0 and 9)
		int no_topics =  RandomHelper.nextIntFromTo(0, 4);
		String toPrint = "Topics to which I subscribed: ";
		for (int i=0; i<=no_topics; i++) {
			int random_topic_id = RandomHelper.nextIntFromTo(0, Options.TOPIC_NUM);
			if (!topics.contains(random_topic_id)) {
				topics.add(random_topic_id);
				toPrint += random_topic_id+ ((i == no_topics)? "":", ");
			}
		}
		
		Oracle.mLog(this.getAddress(), "MulticastProtocol", toPrint);
		multicast = new MulticastProtocol(this, topics);
		
		this.receivedNew = false;
	}


	/**
	 * Returns the MulticastProtocol instance that is handling all the multicast
	 * events on the machine on which this protocol is running.
	 * @return
	 */
	public MulticastProtocol getMulticast() {
		return this.multicast;
	}
	

	/**
	 * Returns the UnicastProtocol instance that is handling all the unicast
	 * events on the machine on which this protocol is running.
	 * @return
	 */
	public UnicastProtocol getUnicast() {
		return this.p2p;
	}
	
	/**
	 * Gets the log of this instance of the protocol.
	 * @return
	 */
	public HashMap<Address, ArrayList<Soliton>> getLog(){
		return this.log;
	}
	
	/**
	 * This method implements the on_sense method of Relay_III. It handles
	 * a Soliton containing some data, by processing and forwarding it,
	 * if it is a new message.
	 * @param d
	 * @param soliton
	 */
	@Protocol.NetworkMessageHandler(dataCls=Soliton.class)
	public void handleSoliton(Address d, Soliton soliton) {
		
		Address source = soliton.getSource();
		int seqn = soliton.getSeqn();

		// New address encountered, initialise list of messages
		if (!this.log.containsKey(source)) {
			
			ArrayList<Soliton> perSourceHistory = new ArrayList<>();
			this.log.put(source, perSourceHistory);

			// Also update the list of peers in Unicast
			this.p2p.addPeer(source);
		}
		
		int expectedSeqn = this.getSourceNextSeqn(source);
		
		//System.out.println("--- Expected seqn per source : " + source + ", " + expectedSeqn);
		if ( expectedSeqn == seqn) {
			this.receivedNew = true;
			
			Oracle.mLog(this.getAddress(), "RealProtocol", "Received new message from " + source + " Forwarding and processing ... ");

			// Forward & add to log
			this.runningOn.broadcast(soliton); 
			this.log.get(source).add(soliton);
			
			// For analysis: Check if some old request was served
			ARQSoliton forThisReply = new ARQSoliton(source, seqn);
			if (toServe.contains(forThisReply)) {
				toServe.remove(forThisReply);
			}
			
			// Handle ProtocolMsg differently
			ProtocolMsg msg = soliton.getData();
			if ( msg instanceof UnicastMsg ) { 

				//System.out.println("----------------Sending Unicast message to Handler ... ");
				p2p.handleSoliton(soliton);
	
			} else if (msg instanceof MulticastMsg) {

				//System.out.println("----------------Sending Multicast message to Handler ... ");
				multicast.handleSoliton(soliton);

			} else {
				// Broadcast type, do stuff for application
				this.application.handle(msg.getAppData());
			}
		} else if (seqn <= expectedSeqn) {
			this.receivedNew = false;
			Oracle.mLog(this.getAddress(), "RealProtocol", "Received old message from " + source);			
		} else {
			this.receivedNew = false;
			Oracle.mLog(this.getAddress(), "RealProtocol", "Received out of order message from " + source);	
		}
	}

	/**
	 * To handle when a node is new and does not know any source. 
	 * It will receive this reply with the complete history per source from neighbours.
	 * For every element in this reply, it calls the handleSoliton() method
	 * @param d
	 * @param reply
	 */
	@Protocol.NetworkMessageHandler(dataCls=ARQHistoryReply.class)
	public void handleARQHistoryReply(Address d, ARQHistoryReply reply) {
		Oracle.mLog(this.getAddress(), "RealProtocol", "Processing history reply for " + reply.getSource());
		
		for (Soliton message : reply.getPerSrcHistory()) {
			this.handleSoliton(d, message);
		}
	}
	
	/**
	 * Handle an ARQSoliton by broadcasting the requested data.
	 * @param d
	 * @param request
	 */
	@Protocol.NetworkMessageHandler(dataCls=ARQSoliton.class)
	public void handleARQSoliton(Address d, ARQSoliton request) {		
		Oracle.mLog(this.getAddress(), "RealProtocol", "Received an ARQSoliton");
		
		Address requestedSrc = request.getSource();
		int requestedSeqn = request.getSeqn();
		
		// Message from new node, reply with complete history per source (including own history)
		if (requestedSrc == null && requestedSeqn == 0) {
			// I have something to communicate
			if (!this.log.isEmpty()) { 
				for(Entry<Address, ArrayList<Soliton>> entry : this.log.entrySet()) {
					
					ARQHistoryReply reply = new ARQHistoryReply(entry.getKey(), entry.getValue());
					this.runningOn.broadcast(reply);
					
				}
			}
		} else {
			// Specific message, per source
			if (this.log.containsKey(requestedSrc)) { //log[P.src]
				
				ArrayList<Soliton> history = this.log.get(requestedSrc);
				
				if (Options.HANDLE_ARQ_OPT) {

					// implementation that sends all messages which are newer than the one requested
					boolean served = false;
					for (int i = 0; i < history.size(); i++) {
						
						Soliton entry = history.get(i);
						if (entry.getSeqn() >= requestedSeqn) {
							Oracle.mLog(this.getAddress(), "RealProtocol", "Responded to an ARQSoliton");
						
							this.runningOn.broadcast(entry);
							served = true;
						}					
					}
					// Analysis purposes
					if (!served && !toServe.contains(request)) {
						toServe.add(request);
					}
					
				} else {

					// basic implementation, as Relay 3 (with just an if): only one message is sent
					for (Soliton entry : history) {
						if (entry.getSeqn() == requestedSeqn) {
							this.runningOn.broadcast(entry);
						}
					}
				}
				
			} else if (!toServe.contains(request)) {
				// Add request to list to be served (for analysis)
				toServe.add(request);
			}
		}	
	}
	
	/**
	 * Trigger ARQ pull request when remembered by the simulation.
	 * @param ev
	 */
	@Protocol.LocalEventHandler(cls=TriggerARQ.class)
	public void handleTriggerARQ(TriggerARQ ev) {
		Oracle.mLog(this.getAddress(), "RealProtocol", "Received TriggerARQ");
		
		if (this.log.isEmpty() || (this.log.size() == 1 && this.log.containsKey(this.runningOn.getAddress()) )) {
			// New node, knows no neighbours, or only has own messages in log
			if (Options.ARQnull_OPT) {
				this.runningOn.broadcast(new ARQSoliton(null, 0));
			}
		} else {	
			// Send pull-update request for known sources
			for(Entry<Address, ArrayList<Soliton>> entry : this.log.entrySet()) {
				
				Address source = entry.getKey();
				ARQSoliton updateReq = new ARQSoliton(source, this.getSourceNextSeqn(source));
				this.runningOn.broadcast(updateReq);
				
			}
		}
		
		this.runningOn.scheduleLocalEvent(Oracle.getInstance().getCurrentTimestamp() + Options.ARQ_INTERVAL, 
								  	 	  new TriggerARQ(this.runningOn.getAddress()));
	}

	/**
	 * Broadcasts a message containing Application data.
	 * @param appData
	 */
	public void sendBroadcastMsg(ApplicationData appData) {
		this.sendSoliton(new BroadcastMsg(appData));
	}
	
	/**
	 * Sends a message originating from this machine and adds it to log.
	 * @param ad
	 */
	protected void sendSoliton(ProtocolMsg msg) {
		
		Address localhost = this.runningOn.getAddress();
		Soliton fromHere = new Soliton(localhost, msg, this.mySeqn);
		
		// Forward
		this.runningOn.broadcast(fromHere);
				
		// First message, initialise local history 
		if (this.mySeqn == 0) {

			ArrayList<Soliton> localHistory = new ArrayList<>();
			localHistory.add(fromHere);
			this.log.put(localhost, localHistory);
			
		} else {
			
			this.log.get(localhost).add(fromHere);
		
		}
		
		this.mySeqn++;
		
	}
	
	/**
	 * Next reference per source.
	 * */ 
	public int getSourceNextSeqn(Address source) {
		
		int nextSeqn = 0;
		if (this.log.containsKey(source)) {

			ArrayList<Soliton> all = this.log.get(source);
			int len = all.size(); 
			if (len != 0)
				nextSeqn = all.get(len-1).getSeqn() + 1;
		
		} 		
		return nextSeqn;
	}
	
	public boolean getNewReceived() {
		return this.receivedNew;  
	}
}
