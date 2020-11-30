package analysis;

import relay.Relay;
import repast.simphony.data2.AggregateDataSource;

public class GeneratedDataSource implements AggregateDataSource {

  static int numberOfGeneratedWaves;

  public GeneratedDataSource() {
    super();
    numberOfGeneratedWaves = 0;
  }

  @Override
  public String getId() {
    return "generatedWaves";
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
    return numberOfGeneratedWaves;
  }

  @Override
  public void reset() {}


}


