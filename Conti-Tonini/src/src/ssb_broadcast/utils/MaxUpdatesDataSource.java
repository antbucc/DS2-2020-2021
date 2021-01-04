package ssb_broadcast.utils;

import repast.simphony.data2.AggregateDataSource;
import ssb_broadcast.Observer;

public class MaxUpdatesDataSource implements AggregateDataSource {

	@Override
	public String getId() {
		return "Maximum updates";
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
			double updates = ((Observer)observer).getAverageUpdates();
			
			if (updates != 0 && updates > max) {
				max = updates;
			}
		}
		
		return max;
	}

	@Override
	public void reset() {
		// Not needed
	}
}
