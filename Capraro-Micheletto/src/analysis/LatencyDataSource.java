package analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import relay.Relay;
import repast.simphony.data2.AggregateDataSource;
import repast.simphony.engine.environment.RunEnvironment;
import waves.ProtocolWave;

public class LatencyDataSource implements AggregateDataSource {

  // Map<Pair<SourceID, reference>, Pair<sendTime, counter>>
  private Map<Pair<Integer, Integer>, Pair<Integer, Integer>> propagatingWaves;

  int sum;

  public LatencyDataSource() {
    super();
    propagatingWaves = new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();
  }

  @Override
  public String getId() {
    return "latency";
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
    int currentTick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

    for (Object obj : objs) {
      Relay relay = (Relay) obj;
      relay.getLastGeneratedWave().stream().forEach(lastGeneratedWave -> {
        this.propagatingWaves.put(
            Pair.of(lastGeneratedWave.getSourceID(), lastGeneratedWave.getReference()),
            Pair.of(currentTick, size - 1));
        GeneratedDataSource.numberOfGeneratedWaves++;
      });
    }

    for (Object obj : objs) {
      Relay relay = (Relay) obj;
      Set<ProtocolWave> receivedWaves = relay.getLastReceivedWaves();
      for (ProtocolWave wave : receivedWaves) {
        Pair<Integer, Integer> pair = Pair.of(wave.getSourceID(), wave.getReference());
        if (this.propagatingWaves.containsKey(pair)) {
          Pair<Integer, Integer> values = this.propagatingWaves.get(pair);
          int counter = values.getRight();
          if (counter > 1) {
            this.propagatingWaves.put(pair, Pair.of(values.getLeft(), counter - 1));
          } else {
            DeliveriesDataSource.numberOfDeliveries++;
            int latency = currentTick - values.getLeft();
            sum += latency;
            MaxLatencyDataSource.maxLatency = Math.max(latency, MaxLatencyDataSource.maxLatency);
            this.propagatingWaves.remove(pair);
          }
        }
      }
    }
    if (DeliveriesDataSource.numberOfDeliveries == 0) {
      return 0;
    }
    return sum / DeliveriesDataSource.numberOfDeliveries;
  }

  @Override
  public void reset() {}

}
