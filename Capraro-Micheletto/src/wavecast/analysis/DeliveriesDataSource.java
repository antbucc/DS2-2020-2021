package wavecast.analysis;

import repast.simphony.data2.AggregateDataSource;
import wavecast.relay.Relay;

public class DeliveriesDataSource implements AggregateDataSource {

  static int numberOfDeliveries;

  public DeliveriesDataSource() {
    super();
    numberOfDeliveries = 0;
  }

  @Override
  public String getId() {
    return "deliveredWaves";
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
    return numberOfDeliveries;
  }

  @Override
  public void reset() {}


}

