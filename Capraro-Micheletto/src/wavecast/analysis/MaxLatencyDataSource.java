package wavecast.analysis;

import repast.simphony.data2.AggregateDataSource;
import wavecast.relay.Relay;

public class MaxLatencyDataSource implements AggregateDataSource {

  static int maxLatency;

  public MaxLatencyDataSource() {
    super();
    maxLatency = 0;
  }

  @Override
  public String getId() {
    return "maxLatency";
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
    return maxLatency;
  }

  @Override
  public void reset() {}


}

