package ssb_broadcast.utils;

import repast.simphony.data2.AggregateDataSource;
import ssb_broadcast.Observer;

public class MinUpdatesDataSource implements AggregateDataSource {

	@Override
	public String getId() {
		return "Minimum updates";
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
			double updates = ((Observer)observer).getAverageUpdates();
			
			if (updates != 0 && min < updates) {
				min = updates;
			}
		}
		
		return min == Double.MAX_VALUE ? 0 : min;
	}

	@Override
	public void reset() {
		// Not needed
	}
}
