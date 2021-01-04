package ssb_broadcast.utils;

import repast.simphony.data2.AggregateDataSource;
import ssb_broadcast.Observer;

public class MaxLatenciesDataSource implements AggregateDataSource {

	@Override
	public String getId() {
		return "Maximum latency";
	}

	@Override
	public Class<?> getDataType() {
		return Double.class;
	}

	@Override
	public Class<?> getSourceType() {
		return Observer.class;
	}

	@Override
	public Object get(Iterable<?> objs, int size) {
		double max = 0;
		
		for (Object observer: objs) {
			double latency = ((Observer)observer).getAverageDelays();
			
			if (latency != 0 && latency > max) {
				max = latency;
			}
		}
		
		return max;
	}

	@Override
	public void reset() {
		// Not needed
	}
}
