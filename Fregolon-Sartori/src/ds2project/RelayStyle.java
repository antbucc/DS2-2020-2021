package ds2project;

import java.awt.Color;
import java.awt.Font;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;

public class RelayStyle extends DefaultStyleOGL2D{
	
	@Override
	public Color getColor(Object agent) {
		Relay1 r = (Relay1) agent;
		if(r.isFailed()) {
			return Color.RED;
		} else {
			return Color.BLUE;
		}
	}
	
	@Override
	public String getLabel(Object object) {
		Relay1 r = (Relay1) object;
		return String.valueOf(r.getId());
	}
	
	@Override
	public Font getLabelFont(Object object) {
		
		return new Font("Arial", Font.PLAIN, 15);
	}
	
	@Override
	public Position getLabelPosition(Object object) {
		return Position.SOUTH;
	}
	
	@Override
	public float getLabelYOffset(Object object) {
		return 5f;
	}


}
