package ssbGossip;

import interfaces.Node;
import repast.simphony.space.graph.EdgeCreator;
import repast.simphony.space.graph.RepastEdge;


public class CustomEdgeCreator<T> implements EdgeCreator<RepastEdge<Node>, Node>{
	@Override
	public Class<CustomEdge> getEdgeType() {
		return CustomEdge.class;
	}

	@Override
	public CustomEdge<Node> createEdge(Node source, Node target, boolean isDirected, double weight) {
		return new CustomEdge<Node>(source, target, isDirected, weight);
	}
}
