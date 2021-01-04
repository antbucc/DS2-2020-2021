package gossipingAppendOnlyLogs.styles;

import gossipingAppendOnlyLogs.actors.Person;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

import java.awt.*;

public class PersonStyle extends DefaultStyleOGL2D {
	@Override
	public Color getColor(Object o) {
		var p = (Person) o;
		var connectedLan = p.getConnectedLAN();
		return connectedLan == null ? Color.GRAY : Color.GREEN;
	}

	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		return shapeFactory.createCircle(2, 3);
	}
	
	@Override
	public Font getLabelFont(Object o) {
		return new Font("TimesRoman", Font.PLAIN, 9);
	}
	
	@Override
	public float getScale(Object o) {
		return 5f;
	}
}