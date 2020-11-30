package ds2.visualization;

import repast.simphony.space.graph.EdgeCreator;

/*
 * Class needed to create edges
 */
public class CustomEdgeCreator<T> implements EdgeCreator<CustomEdge<T>, T> {
	
	@Override
	public Class<CustomEdge> getEdgeType() {
		return CustomEdge.class;
	}

	@Override
	public CustomEdge<T> createEdge(T source, T target, boolean isDirected, double weight) {
		return new CustomEdge<T>(source, target, isDirected, weight);
	}

}
