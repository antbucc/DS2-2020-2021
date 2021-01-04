package gossipingAppendOnlyLogs.models;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Frontier implements Iterable<FrontierItem> {
	private Set<FrontierItem> frontierItemSet = new HashSet<>();

	public void addFrontierItem(FrontierItem item) {
		frontierItemSet.add(item);
	}

	public int getSize() {
		return frontierItemSet.size();
	}

	public FrontierItem getItem(int i) {
		return (FrontierItem) frontierItemSet.toArray()[i];
	}

	@Override
	public Iterator<FrontierItem> iterator() {
		return frontierItemSet.iterator();
	}

	@Override
	public void forEach(Consumer<? super FrontierItem> action) {
		frontierItemSet.forEach(action);
	}

	@Override
	public Spliterator<FrontierItem> spliterator() {
		return frontierItemSet.spliterator();
	}
}
