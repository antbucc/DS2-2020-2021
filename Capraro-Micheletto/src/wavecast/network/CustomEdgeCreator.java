package wavecast.network;

import repast.simphony.space.graph.EdgeCreator;

public class CustomEdgeCreator<T> implements EdgeCreator<CustomEdge<T>, T> {

  public Class<CustomEdge> getEdgeType() {
    return CustomEdge.class;
  }

  public CustomEdge<T> createEdge(T source, T target, boolean isDirected, double weight) {
    return new CustomEdge<T>(source, target, isDirected, weight);
  }

}
