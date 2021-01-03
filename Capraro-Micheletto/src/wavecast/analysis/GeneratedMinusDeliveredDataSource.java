package wavecast.analysis;

import repast.simphony.data2.AggregateDataSource;
import wavecast.relay.Relay;

public class GeneratedMinusDeliveredDataSource implements AggregateDataSource {

  static int difference;

  public GeneratedMinusDeliveredDataSource() {
    super();
    difference = 0;
  }

  @Override
  public String getId() {
    return "generatedMinusDelivered";
  }

  @Override
  public Class<?> getDataType() {
    return Integer.class;
  }

  @Override
  public Class<?> getSourceType() {
    return Relay.class;
  }

  @Override
  public Object get(Iterable<?> objs, int size) {
    return difference;
  }

  @Override
  public void reset() {}


}


