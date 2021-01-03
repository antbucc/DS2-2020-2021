package wavecast.network;

import repast.simphony.space.graph.RepastEdge;

public class CustomEdge<T> extends RepastEdge<T> {

  private boolean showInNetwork;

  public boolean isShowInNetwork() {
    return showInNetwork;
  }

  public void setShowInNetwork(boolean showInNetwork) {
    this.showInNetwork = showInNetwork;
  }

  public CustomEdge(T source, T target, boolean directed, double weight) {
    super(source, target, directed, weight);
  }

  public CustomEdge(T source, T target, boolean directed) {
    super(source, target, directed);
  }
}
