package ds2.visualization;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;

/* 
 * Style descriptor for circles;
 * To get radius and colors for the circle elements,
 * this class is calling methods from DisplayManager when needed
 */
public class StyleCircle2D extends DefaultStyleOGL2D {
	
	private ShapeFactory2D shapeFactory;
	
	@Override
	public void init(ShapeFactory2D factory) {
		this.shapeFactory = factory;
	}
	
	@Override
	public Color getColor(Object agent) {
		return DisplayManager.getInstance().getNextColor("Circle");
	}
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		if (spatial == null) {
			float radius = DisplayManager.getInstance().getNextRadius();
	    	spatial = shapeFactory.createCircle(radius, 40);
	    }
	    return spatial;
	}
	
	@Override
	public int getBorderSize(Object agent) {
		return 2;
	}
	
}
