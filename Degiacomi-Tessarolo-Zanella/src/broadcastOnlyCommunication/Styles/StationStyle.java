package broadcastOnlyCommunication.Styles;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class StationStyle extends DefaultStyleOGL2D {
	@Override
	public Color getColor(Object o) {
		return Color.RED;
	}

	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		return shapeFactory.createCircle(10, 5);
	}
}