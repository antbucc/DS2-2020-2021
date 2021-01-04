package ds2.visualization;

// Java libraries
import java.awt.Color;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Repast libraries
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;

// Custom libraries
import ds2.utility.Options;
import ds2.network.NetworkMessage;
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.protocol.ssb.ProtocolMessage;
import ds2.simulator.Oracle;

public class DisplayManager {

	public static DisplayManager INSTANCE = null;
	public Oracle oracle = null;
	
	// Objects where to place graphic elements
	Context<Object> context = null;
	ContinuousSpace<Object> space = null;
	Network<Object> network;
	
	// Graphic elements:
	CustomEdge<?> theEdge = null;
	ArrayList<CustomCross> crosses = null;
	CustomOrigin theOrigin = null;
	CustomContent theContent = null;
	
	public static DisplayManager getInstance() {
		return DisplayManager.INSTANCE;
	}
	
	// Initialization
	public void init(Context<Object> context) {
		
		DisplayManager.INSTANCE = this;
		this.context = context;
		this.oracle = Oracle.getInstance();
		
		NetworkBuilder<Object> netMsgBuilder = new NetworkBuilder<Object>("network messages", context, true);
		
		netMsgBuilder.setEdgeCreator(new CustomEdgeCreator<>());
		netMsgBuilder.buildNetwork();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace("space",
														context,
														new RandomCartesianAdder<Object>(),
														new repast.simphony.space.continuous.InfiniteBorders<>(), //new repast.simphony.space.continuous.StrictBorders(),
														Options.DISPLAY_SIZE,
														Options.DISPLAY_SIZE);
		
		this.network = (Network<Object>)context.getProjection("network messages");
		this.crosses = new ArrayList<CustomCross>();
	}
	
	public void moveToSpace(Machine<?,?> m, double ax, double ay) {
		this.space.moveTo(m, ax, ay);
	}
	
	// Main function called from Oracle
	
	public void graphic_propagation(NetworkMessage<ProtocolMessage<?>> net_msg) {
		Address src_addr = net_msg.getSource();
		Address dst_addr = net_msg.getDestination();
		
		Machine<?,?> src = oracle.getMachine(src_addr);
		Machine<?,?> dst = oracle.getMachine(dst_addr);

		//System.out.println("NetworkMessage from " + src_addr + " to " + dst_addr);
		
		ProtocolMessage<?> content = net_msg.getData();
		
		if ((src==null) || (dst==null)) {
			return;
		}
		
		// Two identities communicating from the same machine
		if (src.equals(dst)) {
			this.drawOrigin(src);
		}
		else {
			switch (content.getType()) {
				case UPDATE_INIT: {
					this.drawEdge(src, dst, true, 2, Color.GREEN);
				}; break;
				
				case FRONTIER: {
					this.drawEdge(src, dst, true, 2, Color.CYAN);
				}; break;
				
				case NEWS: {
					this.drawEdge(src, dst, true, 2, Color.BLUE);
				}; break;
				
				default: {
					System.err.println("Network message content type not identified");
				}; break;
			}
		}
	}

	
	// --- REMOVE FUNCTIONS --- //
	
	public void freeSpace() {
		this.removeAllEdges();
		this.removeCrosses();
		this.removeOrigin();
		this.removeContent();
	}
	
	public void removeAllEdges() {
		this.network.removeEdges();
		this.theEdge = null;
	}
	
	public void removeCrosses() {
		int size = this.crosses.size();
		if (size != 0) {
			for (int i=0; i<size; i++) {
				this.context.remove(this.crosses.get(i));
			}
			this.crosses.clear();
		}
	}
	
	public void removeOrigin() {
		if (this.theOrigin != null) {
			this.context.remove(this.theOrigin);
		}
		this.theOrigin = null;
	}
	
	public void removeContent() {
		if (this.theContent != null) {
			this.context.remove(this.theContent);
		}
		this.theContent = null;
	}
	
	
	// --- DRAWING FUNCTIONS --- //
	
	public void drawEdge(Machine<?,?> src, Machine<?,?> dst, boolean direct, double weight, Color color) {
		CustomEdge edge = new CustomEdge(src, dst, direct, weight);
		edge.setColor(color);
		
		this.network.addEdge(edge);
		this.theEdge = edge;
	}
	
	public void drawCross(Machine<?,?> dst) {
		double ax = dst.getPosX();
		double ay = dst.getPosY();
	
		CustomCross cross = new CustomCross(ax, ay);
		this.context.add(cross);
		this.space.moveTo(cross, ax * Options.DISPLAY_SIZE, ay * Options.DISPLAY_SIZE);
		this.crosses.add(cross);
	}
	
	public void drawOrigin(Machine<?,?> original_src) {
		double ax = original_src.getPosX();
		double ay = original_src.getPosY();
	
		CustomOrigin origin = new CustomOrigin(ax, ay);
		this.context.add(origin);
		this.space.moveTo(origin, ax * Options.DISPLAY_SIZE, ay * Options.DISPLAY_SIZE);
		this.theOrigin = origin;
	}
	
	public void drawContent(Machine<?,?> src) {
		double ax = src.getPosX();
		double ay = src.getPosY();
	
		CustomContent content = new CustomContent(ax, ay);
		this.context.add(content);
		this.space.moveTo(content, ax * Options.DISPLAY_SIZE, ay * Options.DISPLAY_SIZE);
		this.theContent = content;
	}
	
	public void showFollow(Machine<?,?> src, Machine<?,?> followed) {
		if ((src!=null) && (followed!=null)) {
			this.drawEdge(src, followed, true, 2, Color.ORANGE);
		}
	}
	
	public void showUnFollow(Machine<?,?> src, Machine<?,?> unfollowed) {
		if ((src!=null) && (unfollowed!=null)) {
			this.drawCross(unfollowed);
			this.drawEdge(src, unfollowed, true, 2, Color.ORANGE);
		}
	}
	
	public void showBlock(Machine<?,?> src, Machine<?,?> blocked) {
		if ((src!=null) && (blocked!=null)) {
			this.drawEdge(src, blocked, true, 2, Color.RED);
		}
	}
	
	public void showUnBlock(Machine<?,?> src, Machine<?,?> unblocked) {
		if ((src!=null) && (unblocked!=null)) {
			this.drawCross(unblocked);
			this.drawEdge(src, unblocked, true, 2, Color.RED);
		}
	}
	
	
	// --- AUXILIARY FUNCTIONS --- //
	
	// Method to return the color of the edge
	public Color getNextColor() {
		Color color = null;
		
		if (this.theEdge != null) {
			color = this.theEdge.getColor();
		}
		if (color==null) {
			System.err.println("--- NOT ABLE TO GET EDGE COLOR ---");
		}
		
		return color;
	}
	
	// Function to get the edge's weight value
	public int getEdgeWeight() {
		int weight = -1;
		
		if (this.theEdge != null) {
			weight = (int)this.theEdge.getWeight();
		}
		if (weight==-1) {
			System.err.println("--- NOT ABLE TO GET EDGE WEIGHT ---");
		}
		
		return weight;
	}

}
