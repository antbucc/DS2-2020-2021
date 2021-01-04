package progettoDS2;

import progettoDS2.actors.ConcreteRelay;
import progettoDS2.actors.Relay;

import java.util.ArrayList;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.InfiniteBorders;
import repast.simphony.space.grid.SimpleGridAdder;

public class ContextFactory implements ContextBuilder<Object> {

  int relayCount = 10;
  int size = 40;
  double relayRange = 2;
  String mapKind = "";
  int clumpCount;
  Medium m;
  
  int max(int f, int s) { return f > s ? f : s; }
  int min(int f, int s) { return f < s ? f : s; }
  
  void loadParameters() {
    Parameters p = RunEnvironment.getInstance().getParameters();
    relayCount = p.getInteger("relay_count");
    size = p.getInteger("size");
    mapKind = p.getString("map_kind");
    relayRange = p.getDouble("relay_range");
    clumpCount = p.getInteger("clump_count");    
  }
  
  int[] getRngPointsSize() {
    return new int[] {RandomHelper.nextIntFromTo(0, size-1), RandomHelper.nextIntFromTo(0, size-1)};
  }
  
  int[] fixSize(int[] in) {
    return new int [] {max(0, min(in[0], size -1)), max(0, min(in[1], size -1))};
  }
  
  double getDist(int[] a, int[] b) {
    return Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2));
  }
  
  int[] fixRange(int[] ref, int[] in) {
    double dist = getDist(ref, in); 
    int[] to_r = in;
    while(dist > relayRange) {
      int dim = RandomHelper.nextIntFromTo(0, 1);
      if(ref[dim] < to_r[dim]) {
        to_r[dim]--;
      }else if(ref[dim] > to_r[dim]) {
        to_r[dim]++;
      }
      dist = getDist(ref, to_r);
    }
    return in;
  }
  
  int[] getNextClump(ArrayList<int[]> existingNodes) {
    return getRngPointsSize();
  }
  
  ArrayList<int[]> getClamped(){
    ArrayList<int[]> to_r = new ArrayList<>();
    for(int clamp = 0; clamp < clumpCount; clamp++) {
      int [] clampPos = getNextClump(to_r);
      to_r.add(clampPos);
      for(int i = 0; i <= (int)Math.ceil((relayCount - clumpCount) / clumpCount); i++) {
        int dx = RandomHelper.nextIntFromTo(-(int)relayRange, (int)relayRange);
        int dy = RandomHelper.nextIntFromTo(-(int)relayRange, (int)relayRange);
        
        int [] node_pos = fixRange(clampPos, fixSize(new int[] {
          clampPos[0] + dx,
          clampPos[1] + dy
        }));
        to_r.add(node_pos);
      }
    }
    return to_r;
  }
  
  ArrayList<int[]> getPositions(){ //ofc no touple java xd
    ArrayList<int[]> to_r = new ArrayList<>();
    if(mapKind.equals("uniform")) { //TODO: wow strings rlly? 
      for(int i = 0; i < relayCount; i++) {
        to_r.add(getRngPointsSize());
      }
    }else if(mapKind.equals("clamped")) {
      to_r = getClamped();
    }else if(mapKind.equals("weak_link")) {
      assert(false);
    }else {
      assert(false);
    }
    return to_r;
  }
  
  @Override
  public Context<Object> build(Context<Object> context) {
    context.setId("ProgettoDS2");
    loadParameters();
    NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("FrenNetwork", context, true);
    Network<Object> net = netBuilder.buildNetwork();
    
    GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
    Grid<Object> grid = gridFactory.createGrid("grid", context,
        new GridBuilderParameters<Object>(
            new InfiniteBorders<Object>(), new SimpleGridAdder<Object>(), true, size, size
        )
    );

    this.m = new Medium(context, net, grid);
    ArrayList<int[]> pos = getPositions();
    for (int i = 0; i < pos.size(); i++) {
      Relay r = new ConcreteRelay(m);
      context.add(r);
      int x = pos.get(i)[0];
      int y = pos.get(i)[1];
      grid.moveTo(r, x, y);
      r.setPosition(x,y);
    }
    m.fetchRelays();
    context.add(net);
    if (RunEnvironment.getInstance().isBatch()) {
      RunEnvironment.getInstance().endAt(100);
    }

    return context;
  }
  
  public void buildFromSpec(String path) throws Exception {
    /*try (BufferedReader br = new BufferedReader(new FileReader(path))) {
      String line;
      MGEngine e = MindG.engine()
      e.addEnt("actors.Relay", "actors.TestRelay");
      
      while ((line = br.readLine()) != null) {
        e.parse(line)
      }
      
      e.yield()
    }*/
  }
}