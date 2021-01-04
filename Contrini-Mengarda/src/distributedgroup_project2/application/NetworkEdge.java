package distributedgroup_project2.application;

import repast.simphony.space.graph.RepastEdge;

public class NetworkEdge extends RepastEdge<Object> {
	// Types of edges in the network
	enum EdgeType {
		FOLLOW,
		BLOCK
	}
	
	private final EdgeType type;

	/**
	 * Constructor of the class NetworkEdge.
	 * 
	 * @param type: the type of the edge;
	 * @param source: the source agent of the edge;
	 * @param target: the target agent of the edge.
	 */
	public NetworkEdge(EdgeType type, Object source, Object target) {
		super(source, target, true);
		this.type = type;
	}

	/**
	 * Method to retrieve the type of the edge.
	 *
	 * @return: the type of the edge.
	 */
	public EdgeType getType() {
		return type;
	}
}
