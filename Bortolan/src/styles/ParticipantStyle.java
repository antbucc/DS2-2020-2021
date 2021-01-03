package styles;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import ssbgossip.Participant;

public class ParticipantStyle extends DefaultStyleOGL2D {

	@Override
	public Color getColor(Object object) {
		Participant a;
		if (object instanceof Participant) {
			a = (Participant) object;
			if (a.getType() == Participant.PartType.READY)
				return Color.MAGENTA;
			else if (a.getType() == Participant.PartType.DISCONNECTED)
				return Color.GRAY;
		}
		return Color.BLACK;
	}

}
