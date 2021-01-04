package progettoDS2;

import progettoDS2.actors.Relay;
import progettoDS2.styles.ColoredEdge;
import progettoDS2.utils.BuffMessage;
import progettoDS2.utils.ValueStorage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

@AgentAnnot(displayName = "Medium")
public class Medium {
  /**
   * Set of actors in the system
   */
  Context<Object> context;
  /**
   * A network rapprensetation of the system
   */
  Network<Object> network;
  /**
   * A grind rappresentation of the system
   */
  Grid<Object> grid;
  /**
   * A map associates each relay with its identity
   */
  Map<List<Byte>, Relay> idRelayMap;
  Map<byte[], Color> idColor;
  List<BuffMessage> sendBuffer;
  Map<byte[], ValueStorage> latencyStorage;
  Map<byte[], ValueStorage> jitterStorage;
  /**
   * Used in some simulations to determine the actual system time
   */
  Integer globalClock; 
  Double probMsgLost;
  Double relayRange;
  
  Color justDeliveredEdgeColor = Color.green;
  Color sendingEdgeColor = Color.blue;
  Color lostEdgeColor = Color.red;
  Color arqEdgeColor = Color.yellow;
  /**
   * Builds a medium that can deliver messages between relays and updates the graphs
   * @param ctx The context
   * @param net The network between the relays
   * @param grid The grid rappresentation of the positions
   */
  public Medium(Context<Object> ctx, Network<Object> net, Grid<Object> grid) {
    this.context = ctx;
    this.context.add(this);
    this.network = net;
    this.idRelayMap = new HashMap<>();
    this.latencyStorage = new HashMap<>();
    this.jitterStorage = new HashMap<>();
    this.sendBuffer = new ArrayList<>();
    this.globalClock = 0;
    this.grid = grid;
    this.idColor = new HashMap<>();
    loadParameters();
  }
  /**
   * Loads the simulator parameters
   */
  void loadParameters() {
    Parameters p = RunEnvironment.getInstance().getParameters();
    probMsgLost = p.getDouble("prob_msg_lost");
    relayRange = p.getDouble("relay_range");
  }
  /**
   * Fetches all the relays in the context
   */
  public void fetchRelays() {
    for(Object o : this.context) {
      if(o instanceof Relay) {
        idRelayMap.put(((Relay)o).getIdentity(), (Relay)o);
      }
    }
  }
  
  @ScheduledMethod(start = 1, interval = 1)
  public void tick() {
    this.globalClock ++;
    updateGraphs();
    deliverMessages();
  }
  /**
   * Deliver all messages that are due to this cycle and filter the message
   * queue
   */
  void deliverMessages() {
    sendBuffer.stream().forEach(msg ->{
      msg.DeliverIn -= 1;
      if(msg.DeliverIn == 0) {
        msg.Lost = !(RandomHelper.nextDoubleFromTo(0, 1) >= probMsgLost);
        if(!msg.Lost) {
          idRelayMap.get(msg.Dst).onSense(msg.Msg);
        }
      }
    });
    sendBuffer.stream().filter(msg -> ((!msg.Lost) && msg.DeliverIn>0));
  }
  /**
   * Updated the graphs for the current cycle
   */
  void updateGraphs() {
    updateNetwork();
  }
  /**
   * Updates the network graph
   */
  void updateNetwork() {
    drawSending(); 
  }
  /**
   * Draws all outgoing messages 
   */
  void drawSending() {
    ArrayList<ColoredEdge> edges = new ArrayList<>();
    for(BuffMessage s : this.sendBuffer) {
      if(s.DeliverIn == 0) {
        ColoredEdge edge = new ColoredEdge(
          idRelayMap.get(s.Src), idRelayMap.get(s.Dst));
        edges.add(edge);      
      }
    }
    this.network.removeEdges();
    for(ColoredEdge ro : edges) {
      this.network.addEdge((RepastEdge<Object>)ro);  
    }    
  }
  /**
   * Tells if o1 is in range of o2
   * @param o1 The first relay
   * @param o2 The second relay
   * @return true if in range, else false
   */
  boolean canReach(Object o1, Object o2) {
    GridPoint p1 = grid.getLocation(o1);
    GridPoint p2 = grid.getLocation(o2);
    return (grid.getDistance(p1, p2) <= relayRange); 
  }
  /**
   * Inserts a message in the network, taking into consideration the
   * relay range
   * @param msg The message to be sent
   * @param sender The range
   */
  public void send(Object msg, Relay sender) { 
    // for now no range on send
    for(Relay r : this.idRelayMap.values()) {
      if(canReach(r, sender)) {
        this.sendBuffer.add(new BuffMessage(sender.getIdentity(), r.getIdentity(), msg, 1));
      }
    }
  }
  /**
   * Cheat method used for some evaluations, returns a "true"
   * global clock
   * @return The ammount of ticks since simulation start
   */
  Integer getGlobalClock() {
    return globalClock;
  }
  //FIXME: Drawing is bugged, fixme
  public double getAvgLatency() {
    Double cumul = 0.0;
    Integer count = 0;
    for(ValueStorage ls : latencyStorage.values()) {
      count ++;
      cumul += ls.getValue();
    }
    if(count == 0) { return 0; }
    return (cumul / (double)count);
  }
  
  public double getAvgJitter() {
    Double cumul = 0.0;
    Integer count = 0;
    for(ValueStorage js : jitterStorage.values()) {
      count ++;
      cumul += js.getValue();
    }
    if(count == 0) { return 0; }
    return (cumul / (double)count);
  }
  
  public double getMaxLatency() {
    double ml = 0.0;
    for(ValueStorage ls : latencyStorage.values()) {
      Double lat = ls.getMaxValue();
      if(lat > ml) { ml = lat; }
    }    
    return ml;
  }

}
