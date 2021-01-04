package gossipingAppendOnlyLogs.styles;

import gossipingAppendOnlyLogs.actors.LAN;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

import java.awt.*;

public class LANStyle extends DefaultStyleOGL2D {
	@Override
	public Color getColor(Object o) {
		Color color = Color.ORANGE;
		
		int alpha = 150;
		
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		var lan = (LAN) agent;
		return shapeFactory.createCircle((float) lan.maximumDistance, 30);
	}
	
	@Override
	public Font getLabelFont(Object o) {
		return new Font("TimesRoman", Font.PLAIN, 9);
	}
	
	@Override
	public float getScale(Object o) {
		return 15f;
	}
}