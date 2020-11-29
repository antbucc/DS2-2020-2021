package distributedgroup_project1.data_sources;

import distributedgroup_project1.Relay;
import repast.simphony.data2.AggregateDataSource;

public class LatencyMaxDataSource implements AggregateDataSource {

	@Override
	public String getId() {
		return "latency_max";
	}

	@Override
	public Class<?> getDataType() {
		return Double.class;
	}

	@Override
	public Class<?> getSourceType() {
		return Relay.class;
	}

	@Override
	public Object get(Iterable<?> objs, int size) {
		double max = 0;
		
		// Compute the maximum of the average latencies for each relay,
		// making sure to skip zeros (which means that the relay has still to receive any perturbation). 

		for (Object relay : objs) {
			double relaylatency = ((Relay) relay).getAverageLatency();
			
			if (relaylatency != 0) {
				max = Math.max(max, relaylatency);				
			}
		}

		return max;
	}

	@Override
	public void reset() {
	}

}
