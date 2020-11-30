package ds2.visualization;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CustomCircle {
	
	double posX;
	double posY;
	float radius;
	
	String currentColor;
	Map<String, Color> availableColors;
	
	public CustomCircle(double posX, double posY, float radius, String color) {
		this.posX = posX;
		this.posY = posY;
		this.radius = radius;
		this.currentColor = color;
		
		this.availableColors = new HashMap<String, Color>();
		this.availableColors.put(	"Green", 	new Color(223, 255, 230));
		this.availableColors.put(	"Orange", 	new Color(250, 240, 195));
		this.availableColors.put(	"Pink", 	new Color(240, 230, 255));
		this.availableColors.put(	"DEBUG", 	new Color(0, 0, 0));
	}
	
	public void setX(double newX) {
		this.posX = newX;
	}
	
	public void setY(double newY) {
		this.posY = newY;
	}
	
	public void setRadius(float newRadius) {
		this.radius = newRadius;
	}
	
	public float getRadius() {
		return this.radius;
	}
	
	public void setColor(String newColor) {
		this.currentColor = newColor;
	}

	public Color getColor() {
		Color res = this.availableColors.get(this.currentColor); 
		if (res==null) {
			System.err.println("--THIS CIRCLE COLOR DOESN'T EXIST, ASSIGNING BLACK--");
			res = this.availableColors.get("DEBUG");
		}
		return res;
	}
}
