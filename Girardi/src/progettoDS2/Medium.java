package progettoDS2;

import progettoDS2.styles.ColoredEdge;

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

class BuffMessage {
  String Src;
  String Dst;
  Object Msg;
  boolean Lost;
  Integer DeliverIn;
  public BuffMessage(String src, String dst, Object msg, Integer deliverIn, boolean lost) {
    Src = src;
    Dst = dst;
    Msg = msg;
    DeliverIn = deliverIn;
    Lost = lost;
  }
}

class ValueStorage {
  int count;
  double cumul;
  double maxValue;
  double minValue;
  boolean firstValue;
  public ValueStorage() {
    count = 0;
    cumul = 0.0;
    firstValue = true;
  }
  public void addCumul(double value) {
    cumul += value;
    count ++;
    if(firstValue) {
      maxValue = value;
      minValue = value;
      firstValue = false;
    }
    maxValue = maxValue > value ? maxValue : value;
    minValue = minValue < value ? minValue : value;
  }
  public double getValue() {
    return count != 0 ? cumul / (double)count : 0;
  }
  public double getMaxValue() {
    return maxValue;
  }
  public double getMinValue() {
    return minValue;
  }
}


@AgentAnnot(displayName = "Medium")
public class Medium {
  Context<Object> context;
  Network<Object> network;
  Grid<Object> grid;
  
  Map<String, Relay> idRelayMap;
  Map<String, Color> idColor;
  List<BuffMessage> sendBuffer;
  Map<String, ValueStorage> latencyStorage;
  Map<String, ValueStorage> jitterStorage;
  Integer globalClock; //"god time"
  Double probMsgLost;
  Double relayRange;
  
  Color justDeliveredEdgeColor = Color.green;
  Color sendingEdgeColor = Color.blue;
  Color lostEdgeColor = Color.red;
  Color arqEdgeColor = Color.yellow;
  
  Color getColorOfRelay(String id) {
    if(idColor.containsKey(id)) {
      return idColor.get(id);
    }
    double hHigh = 200;
    double hLow = 80;
    float s = (float)RandomHelper.nextDoubleFromTo(30, 100);
    float v = (float)RandomHelper.nextDoubleFromTo(90, 100);
    float h = (float)RandomHelper.nextDoubleFromTo(hLow, hHigh);
    Color c = Color.getHSBColor(h, s, v);
    idColor.put(id,c);
    return c;
  }
  
  public ColoredEdge getColorFromBuffMessage(BuffMessage s) {
    Relay start = idRelayMap.get(s.Src);
    Relay end = idRelayMap.get(s.Dst);
    if(!canReach(start, end)) {
      return null;
    }
    Color c = null;
    if(!s.Lost && s.DeliverIn == 0) {
      if(s.Msg instanceof PtpMessage) {
        c = getColorOfRelay(((PtpMessage)s.Msg).getFor());
      }else {
        c = null;//arqEdgeColor;
      }
    }else if(s.Lost && s.DeliverIn == 0) {
      c = null;//lostEdgeColor; 
    }else if(!s.Lost && s.DeliverIn == -1) {
      c = null;// justDeliveredEdgeColor;
    }
    if(c != null) {
      return new ColoredEdge(start, end, false, c); 
    }
    return null;
  }
  
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
  void loadParameters() {
    Parameters p = RunEnvironment.getInstance().getParameters();
    probMsgLost = p.getDouble("prob_msg_lost");
    relayRange = p.getDouble("relay_range");
  }
  
  public void fetchRelays() {
    for(Object o : this.context) {
      if(o instanceof Relay) {
        idRelayMap.put(((Relay)o).identity, (Relay)o);
      }
    }
  }
  
  @ScheduledMethod(start = 1, interval = 1)
  public void tick() {
    this.globalClock ++;
    updateGraphs();
    deliverMessages();
  }
  
  void deliverMessages() {
    List<BuffMessage> postman = new ArrayList<>(sendBuffer);
    sendBuffer.clear();
    for (BuffMessage msg : postman) {
      msg.DeliverIn -= 1;
      if(msg.DeliverIn == 0) {
        msg.Lost = !(RandomHelper.nextDoubleFromTo(0, 1) >= probMsgLost);
      }
      if(!msg.Lost && msg.DeliverIn == 0 &&
          canReach(idRelayMap.get(msg.Dst), idRelayMap.get(msg.Src))) 
      {
          idRelayMap.get(msg.Dst).onSense(msg.Msg);
      }
      if(msg.DeliverIn > -2) {
        sendBuffer.add(msg);
      }
    }
  }
  
  void updateGraphs() {
    updateNetwork();
  }
  
  void updateNetwork() {
    drawSending(); //overwrite with sending if any sending
    //System.out.println("----Done----");
  }
  void drawSending() {
    ArrayList<ColoredEdge> edges = new ArrayList<>();
    for(BuffMessage s : this.sendBuffer) {
      //System.out.println("Drawing sending");
      ColoredEdge edge = getColorFromBuffMessage(s);
      if(edge != null) {
        edges.add(edge);
        //this.network.addEdge((RepastEdge<Object>)edge);        
      }
    }//first is lower
    Color[] prio = new Color[] {sendingEdgeColor};
    this.network.removeEdges();
    for(ColoredEdge ro : edges) {
      this.network.addEdge((RepastEdge<Object>)ro);   
    }
    
  }
  
  void testRandomSend() {
    Object tmp = this.context.getRandomObject();
    while(!(tmp instanceof Relay)) { tmp = this.context.getRandomObject(); }
    ((Relay) tmp).testMessage();
  }
  
  boolean canReach(Object o1, Object o2) {
    GridPoint p1 = grid.getLocation(o1);
    GridPoint p2 = grid.getLocation(o2);
    return (grid.getDistance(p1, p2) <= relayRange); 
  }
  public void send(Object msg, Relay sender) { 
    // for now no range on send
    for(Relay r : this.idRelayMap.values()) {
      if(canReach(r, sender)) {
        this.sendBuffer.add(new BuffMessage(sender.identity, r.identity, msg, 1, false));
      }
    }
  }
  
  Integer getGlobalClock() {
    return globalClock;
  }
  
  void notifyLatency(Integer latency, Relay relay) {
    ValueStorage targetLatency;
    ValueStorage targetJitter;
    if(!latencyStorage.containsKey(relay.identity)) {
      targetLatency = new ValueStorage();
      targetJitter = new ValueStorage();
      latencyStorage.put(relay.identity, targetLatency);
      jitterStorage.put(relay.identity, targetJitter);
    } else {
      targetJitter = jitterStorage.get(relay.identity);
      targetLatency = latencyStorage.get(relay.identity);
    }
    targetJitter.addCumul(Math.abs(targetLatency.getValue() - latency));
    targetLatency.addCumul(latency);
  }
  
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
