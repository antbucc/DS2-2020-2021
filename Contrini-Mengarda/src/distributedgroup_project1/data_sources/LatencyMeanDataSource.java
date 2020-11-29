package distributedgroup_project1.data_sources;

import distributedgroup_project1.Relay;
import repast.simphony.data2.AggregateDataSource;

public class LatencyMeanDataSource implements AggregateDataSource {

	@Override
	public String getId() {
		return "latency_mean";
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
		double sum = 0;
		int count = 0;

		// Compute the average of the average latencies for each relay,
		// making sure to skip zeros (which means that the relay has still to receive any perturbation). 
		
		for (Object relay : objs) {
			double currentAverage = ((Relay) relay).getAverageLatency();
			
			if (currentAverage != 0) {
				sum += currentAverage;
				count++;
			}
		}

		return sum / count;
	}

	@Override
	public void reset() {
	}

}
