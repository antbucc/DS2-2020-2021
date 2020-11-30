package styles;

import java.awt.Color;

import bcastonly.Relay;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class RelayStyle extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object object) {
		Relay a;
		if (object instanceof Relay) {
			a = (Relay) object;
			if (a.getType() == Relay.RelayType.QUIETE)
				return Color.BLUE;
			else if (a.getType() == Relay.RelayType.SENDER)
				return Color.RED;
			else if (a.getType() == Relay.RelayType.RELAY)
				return Color.GREEN;
			else if (a.getType() == Relay.RelayType.ARQ_REQ)
				return Color.CYAN;
			else if (a.getType() == Relay.RelayType.DISCONNECTED)
				return Color.GRAY;
		}
		return Color.BLACK;
	}

	@Override
	public float getScale(Object object) {
		Relay a;
		if (object instanceof Relay) {
			a = (Relay) object;
			if (a.getType() == Relay.RelayType.SENDER)
				return 1f * 3;
			return 1f;
		}
		return 1f;
	}
}
