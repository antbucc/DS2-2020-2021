package ds2project;

import repast.simphony.space.graph.EdgeCreator;
import repast.simphony.space.graph.RepastEdge;


public class CustomEdgeCreator<T> implements EdgeCreator<RepastEdge, T>{
	@Override
	public Class<CustomEdge> getEdgeType() {
		return CustomEdge.class;
	}

	@Override
	public CustomEdge<T> createEdge(T source, T target, boolean isDirected, double weight) {
		return new CustomEdge<T>(source, target, isDirected, weight);
	}
}
