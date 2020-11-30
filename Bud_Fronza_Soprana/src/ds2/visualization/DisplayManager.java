package ds2.visualization;

// Java libraries
import java.awt.Color;
import java.util.ArrayList;

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
import ds2.protocol.RealProtocol;
import ds2.protocol.messages.ARQHistoryReply;
import ds2.protocol.messages.ARQSoliton;
import ds2.protocol.messages.Perturbation;
import ds2.protocol.messages.ProtocolMsg;
import ds2.protocol.messages.Soliton;
import ds2.protocol.messages.data.BroadcastMsg;
import ds2.protocol.messages.data.MulticastMsg;
import ds2.protocol.messages.data.UnicastMsg;
import ds2.simulator.Oracle;

public class DisplayManager {

	public static DisplayManager INSTANCE = null;
	public Oracle oracle = null;
	
	// Objects where to place graphic elements
	Context<Object> context = null;
	ContinuousSpace<Object> space = null;
	Network<Object> network;
	
	// Graphic elements
	CustomEdge theEdge = null;
	CustomCircle theCircle = null;
	CustomCross theCross = null;
	CustomOrigin theOrigin = null;
	
	
	public static DisplayManager getInstance() {
		return DisplayManager.INSTANCE;
	}
	
	// Initialization
	public void init(Context<Object> context) {
		
		DisplayManager.INSTANCE = this;
		this.context = context;
		this.oracle = Oracle.getInstance();
		
		NetworkBuilder<Object> netMsgBuilder = new NetworkBuilder<Object>("network messages", context, true);
		
		netMsgBuilder.setEdgeCreator(new CustomEdgeCreator());
		netMsgBuilder.buildNetwork();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		this.space = spaceFactory.createContinuousSpace("space",
														context,
														new RandomCartesianAdder<Object>(),
														new repast.simphony.space.continuous.InfiniteBorders<>(), //new repast.simphony.space.continuous.StrictBorders(),
														Options.DISPLAY_SIZE,
														Options.DISPLAY_SIZE);
		
		this.network = (Network<Object>)context.getProjection("network messages");
	}
	
	
	public NdPoint getDisplayPos(Machine m) {
		return this.space.getLocation(m);
	}
		
	public void moveToSpace(Machine m, double ax, double ay) {
		this.space.moveTo(m, ax, ay);
	}
	
	// Main function called from Oracle
	
	public void graphic_propagation(NetworkMessage net_msg) {
		
		Address original_src_addr = net_msg.getData().getSource();
		Address src_addr = net_msg.getSource();
		Address dst_addr = net_msg.getDestination();

		Machine original_src = oracle.getMachine(original_src_addr);
		Machine src = oracle.getMachine(src_addr);
		Machine dst = oracle.getMachine(dst_addr);

		// If the destination is dead we can't either show the arrow nor the circle
		if (dst == null) {
			return;
		}

		Perturbation data = net_msg.getData();
		
		System.out.println("NetworkMessage from " + src_addr + " to " + dst_addr);
				
		String circleColor = null;
		Color edgeColor;
		
		// Collision or probabilistic loss
		if (!net_msg.isReceived()) {
			this.drawCross(dst);
			edgeColor = Color.RED;
			if (src != null) {
				this.drawEdge(src, dst, 2, edgeColor);
			}
			return;
		}
		else if (data instanceof Soliton) {
			edgeColor = Color.GREEN;
			circleColor = get_PM_color(((Soliton)data).getData());
		}
		else if (data instanceof ARQSoliton) {
			edgeColor = Color.ORANGE;
		}
		else if (data instanceof ARQHistoryReply) {
			edgeColor = Color.MAGENTA;
		}
		else {
			System.err.println("--- NOT ABLE TO DETECT MSG DATA TYPE ---");
			return;
		}
		
		// Destination corresponds to the original source
		if (dst_addr.equals(original_src_addr)) {
			return;
		}
		
		// If the expected message is this, draw the circle
		if (data instanceof Soliton) {
			Soliton soliton = (Soliton)data;
			RealProtocol p = (RealProtocol)dst.getProtocol();
			
			if (p.getNewReceived()) {
				if (original_src != null) {
					this.drawCircle(original_src, dst, circleColor);
				}
			}
		}
		
		// If the re-broadcaster is alive we can show the arrow	
		if (src != null) {
			this.drawEdge(src, dst, 2, edgeColor);
		}
			
		// If the original source is alive we can show the circle and circleColor != null (not an unrecognized data type or a collision)
		if (original_src != null) {
			this.drawOrigin(original_src);
		}
	}
	
	public static String get_PM_color(ProtocolMsg message) {
		if (message instanceof BroadcastMsg) {
			return "Green";
		}
		else if (message instanceof MulticastMsg) {
			return "Orange";
		}
		else if (message instanceof UnicastMsg) {
			return "Pink";
		}
		else {
			System.err.println("--- NOT ABLE TO DETECT PROTOCOL DATA TYPE ---");
			return "DEBUG";
		}
	}
	
	
	// --- REMOVE FUNCTIONS --- //
	
	public void freeSpace() {
		this.removeAllEdges();
		this.removeAllCircles();
		this.removeCross();
		this.removeOrigin();
	}
	
	public void removeAllEdges() {
		this.network.removeEdges();
		this.theEdge = null;
	}
	
	public void removeAllCircles() {
		if (this.theCircle != null) {
			this.context.remove(this.theCircle);
		}
		this.theCircle = null;
	}
	
	public void removeCross() {
		if (this.theCross != null) {
			this.context.remove(this.theCross);
		}
		this.theCross = null;
	}
	
	public void removeOrigin() {
		if (this.theOrigin != null) {
			this.context.remove(this.theOrigin);
		}
		this.theOrigin = null;
	}
	
	
	// --- DRAWING FUNCTIONS --- //
	
	public void drawEdge(Machine src, Machine dst, double weight, Color color) {
		CustomEdge edge = new CustomEdge(src, dst, true, weight);
		edge.setColor(color);
		
		this.network.addEdge(edge);
		this.theEdge = edge;
	}
		
	public void drawCircle(Machine src, Machine dst, String color) {
		double ax = src.getPosX();
		double ay = src.getPosY();
		
		double radius = this.EuclideanDistance(src, dst);
		
		// Conversion due to display parameters
		radius = radius*Options.DISPLAY_SIZE*50;
		
		CustomCircle circle = new CustomCircle(ax, ay, (float)radius, color);
		
		// Add the new circle to the plane
		this.context.add(circle);
		this.space.moveTo(circle, ax * Options.DISPLAY_SIZE, ay * Options.DISPLAY_SIZE);
		this.theCircle = circle;
	}
	
	public void drawCross(Machine dst) {
		double ax = dst.getPosX();
		double ay = dst.getPosY();
	
		CustomCross cross = new CustomCross(ax, ay);
		this.context.add(cross);
		this.space.moveTo(cross, ax * Options.DISPLAY_SIZE, ay * Options.DISPLAY_SIZE);
		this.theCross = cross;
	}
	
	public void drawOrigin(Machine original_src) {
		double ax = original_src.getPosX();
		double ay = original_src.getPosY();
	
		CustomOrigin origin = new CustomOrigin(ax, ay);
		this.context.add(origin);
		this.space.moveTo(origin, ax * Options.DISPLAY_SIZE, ay * Options.DISPLAY_SIZE);
		this.theOrigin = origin;
	}
	
	
	// --- AUXILIARY FUNCTIONS --- //
	
	public double EuclideanDistance(Machine src, Machine dst) {
		double x1 = src.getPosX(); 
		double y1 = src.getPosY();
		double x2 = dst.getPosX();
		double y2 = dst.getPosY();
		
		return (Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1)));
	}
	
	// Returns the color of edges and circles
	public Color getNextColor(String element) {
		Color color = null;
		
		if (element.equals("Edge")) {
			if (this.theEdge != null) {
				color = this.theEdge.getColor();
			}
			if (color==null) {
				System.err.println("--- NOT ABLE TO GET EDGE COLOR ---");
			}
		}
		else if (element.equals("Circle")) {
			if (this.theCircle != null) {
				color = this.theCircle.getColor();
			}
			if (color==null) {
				System.err.println("--- NOT ABLE TO GET CIRCLE COLOR ---");
			}
		}
		return color;
	}
	
	// Returns the circle's radius value 
	public float getNextRadius() {
		float radius = -1;
		
		if (this.theCircle != null) {
			radius = this.theCircle.getRadius();
		}
		if (radius==-1) {
			System.err.println("--- NOT ABLE TO GET CIRCLE RADIUS ---");
		}
		return radius;
	}
	
	// Returns the edge's weight value
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
