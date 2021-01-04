package ssb_broadcast.utils;

import repast.simphony.data2.AggregateDataSource;
import ssb_broadcast.Observer;

public class MinLatenciesDataSource implements AggregateDataSource {

	@Override
	public String getId() {
		return "Minimum latency";
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
		double min = Double.MAX_VALUE;
		
		for (Object observer: objs) {
			double latency = ((Observer)observer).getAverageDelays();
			
			if (latency != 0 && latency < min) {
				min = latency;
			}
		}
		
		return min == Double.MAX_VALUE ? 0 : min;
	}

	@Override
	public void reset() {
		// Not needed
	}
}
