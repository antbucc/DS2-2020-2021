package ssb_broadcast.utils;

import repast.simphony.data2.AggregateDataSource;
import ssb_broadcast.Observer;

public class AverageUpdatesDataSource implements AggregateDataSource {

	@Override
	public String getId() {
		return "Average updates";
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
		double average = 0.0;
		int count = 0;
		
		for (Object observer: objs) {
			average += ((Observer)observer).getAverageUpdates();
			count++;
		}
		
		return average / count;
	}

	@Override
	public void reset() {
		// Not needed
	}
}
