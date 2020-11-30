package ds2.visualization;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

// Repast libraries
import repast.simphony.space.graph.RepastEdge;

public class CustomEdge<T> extends RepastEdge<T> {
	
	double weight;
	Color currentColor;
	
	public CustomEdge(T source, T target, boolean directed, double weight) {
		super(source, target, directed, weight);
		
		this.weight = weight;
	}
	
	public void setColor(Color newColor) {
		this.currentColor = newColor;
	}

	public Color getColor() {
		return this.currentColor;
	}
	
	public void setWeight(double newWeight) {
		this.weight = newWeight;
	}
	
	@Override
	public double getWeight() {
		return this.weight;
	}
}
