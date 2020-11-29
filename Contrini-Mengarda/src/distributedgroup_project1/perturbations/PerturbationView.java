package distributedgroup_project1.perturbations;

import java.awt.Color;
import java.awt.Font;

import distributedgroup_project1.Utils;
import repast.simphony.visualizationOGL2D.StyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class PerturbationView implements StyleOGL2D<Perturbation>{
	
	private ShapeFactory2D factory;
	
	// The initial opacity (alpha) of the perturbation
	private static final int INITIAL_OPACITY = 90;
	
	@Override
	public void init(ShapeFactory2D factory) {
		this.factory = factory;
	}

	@Override
	public VSpatial getVSpatial(Perturbation perturbation, VSpatial spatial) {
		spatial = factory.createCircle((float) perturbation.getRadius(), 100);
		return spatial;
	}

	@Override
	public Color getColor(Perturbation perturbation) {
		// Calculate the new opacity depending on how large the perturbation is.
		// Initially the value will be INITIAL_OPACITY, while at the "end" it will be 0.
		// To manage the fact that the radius can be larger than the maximum radius,
		// we limit the perturbationSizeProportion to 1
		double perturbationSizeProportion = Math.min(perturbation.getRadius() / Utils.MAX_PERTURBATION_RADIUS, 1);
		int alpha = (int) (INITIAL_OPACITY * (1 - (perturbationSizeProportion)));
		
		Color color;
		
		if (perturbation instanceof BroadcastPerturbation) {
			color = new Color(79, 214, 236, alpha); // Light blue
		} else if (perturbation instanceof P2PPerturbation) {
			color = new Color(98, 230, 106, alpha); // Green
		} else if (perturbation instanceof PubSubPerturbation) {
			color = new Color(255, 103, 103, alpha); // Red
		} else if (perturbation instanceof ARQPerturbation) {
			color = new Color(228, 165, 243, alpha); // Pink
		} else if (perturbation instanceof RetransmissionPerturbation) {
			color = new Color(79, 214, 236, alpha); // Light blue
		} else {
			color = null;
		}
		
		return color;
	}

	@Override
	public int getBorderSize(Perturbation perturbation) {
		return 0;
	}

	@Override
	public Color getBorderColor(Perturbation perturbation) {
		return null;
	}

	@Override
	public float getRotation(Perturbation perturbation) {
		return 0;
	}

	@Override
	public float getScale(Perturbation perturbation) {
		return 15F;
	}

	@Override
	public String getLabel(Perturbation perturbation) {
		return null;
	}

	@Override
	public Font getLabelFont(Perturbation perturbation) {
		return null;
	}

	@Override
	public float getLabelXOffset(Perturbation perturbation) {
		return 0;
	}

	@Override
	public float getLabelYOffset(Perturbation perturbation) {
		return 0;
	}

	@Override
	public Position getLabelPosition(Perturbation perturbation) {
		return null;
	}

	@Override
	public Color getLabelColor(Perturbation perturbation) {
		return null;
	}

}
