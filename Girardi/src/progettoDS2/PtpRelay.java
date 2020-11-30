package progettoDS2;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

class PtpMessage extends StdMessage {
  public PtpMessage(String from, Integer ref, String val) {
    super(from, ref, val);
  }
  public String getFor() {
    return Val.split(";")[0];
  }
  public Integer getGTSend() {
    //this is a bit of cheating used to calculate the
    // latency, it does not violate the protocol but it
    // makes testing easier. In the general case it is
    // impossible to have a "God clock" 
    return Integer.parseInt(Val.split(";")[1]);
  }
}

public class PtpRelay extends Relay {
  double wave_probility;
  public PtpRelay(Medium m) {
    super(m);
    loadParameters();
  }
  
  void loadParameters() {
    Parameters p = RunEnvironment.getInstance().getParameters();
    wave_probility = p.getDouble("wave_probability");
  }
  
  @Override
  void applicationReceiveMessage(StdMessage msg) {
    if(msg instanceof PtpMessage && ((PtpMessage)msg).getFor().equals(identity)) {
      //System.out.println("Fren wrote me, yay");
      medium.notifyLatency(medium.getGlobalClock() - ((PtpMessage)msg).getGTSend(), this);
    }else if(msg instanceof PtpMessage) {
     // System.out.print(((PtpMessage)msg).getFor());
    }
  }
  
  String randomFriend() {
    return frends.keySet().stream().toArray(String[]::new)[
      RandomHelper.nextIntFromTo(0, frends.keySet().size() - 1)
    ];
  }
  
  @Override
  void applicationTick() {
    double val = RandomHelper.nextDoubleFromTo(0, 1);    
    if(val <= wave_probility) {
      if(frends.isEmpty()) { //likely
        stdSend(new PtpMessage(this.identity, 0, " ; ")); //send to no one
      }else {
        //System.out.println("We got fren, lets write them");
        String target = randomFriend();
        String value = target + ";" + Integer.toString(medium.getGlobalClock());
        stdSend(new PtpMessage(this.identity, 0, value));
      }
    }  
  }
}
