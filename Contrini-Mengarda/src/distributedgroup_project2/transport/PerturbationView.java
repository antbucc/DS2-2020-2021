package distributedgroup_project2.transport;

import java.awt.Color;
import java.awt.Font;

import distributedgroup_project2.Utils;
import repast.simphony.visualizationOGL2D.StyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;

public class PerturbationView implements StyleOGL2D<Perturbation>{
	
	private ShapeFactory2D factory;
	
	// The initial opacity (alpha) of the perturbation
	private static final int INITIAL_OPACITY = 90;

	/**
	 * Method to initialize the view of a perturbation.
	 * 
	 * @param factory: the factory used to create the perturbation view.
	 */
	@Override
	public void init(ShapeFactory2D factory) {
		this.factory = factory;
	}

	/**
	 * Method to create the perturbation view.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @param spatial: the current representation of the perturbation;
	 * @return: the new representation of the perturbation.
	 */
	@Override
	public VSpatial getVSpatial(Perturbation perturbation, VSpatial spatial) {
		spatial = factory.createCircle((float) perturbation.getRadius(), 100);
		return spatial;
	}

	/**
	 * Method to retrieve the color of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new color of the perturbation. 
	 */
	@Override
	public Color getColor(Perturbation perturbation) {
		// Calculate the new opacity depending on how large the perturbation is.
		// Initially the value will be INITIAL_OPACITY, while at the "end" it will be 0.
		// To manage the fact that the radius can be larger than the maximum radius,
		// we limit the perturbationSizeProportion to 1.
		double perturbationSizeProportion = Math.min(perturbation.getRadius() / Utils.MAX_PERTURBATION_RADIUS, 1);
		int alpha = (int) (INITIAL_OPACITY * (1 - (perturbationSizeProportion)));
		
		return new Color(79, 214, 236, alpha); // Light blue;
	}

	/**
	 * Method to retrieve the border size of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new border size of the perturbation. 
	 */
	@Override
	public int getBorderSize(Perturbation perturbation) {
		return 0;
	}

	/**
	 * Method to retrieve the border color of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new border color of the perturbation. 
	 */
	@Override
	public Color getBorderColor(Perturbation perturbation) {
		return null;
	}

	/**
	 * Method to retrieve the rotation of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new rotation of the perturbation. 
	 */
	@Override
	public float getRotation(Perturbation perturbation) {
		return 0;
	}

	/**
	 * Method to retrieve the scaling factor of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new scaling factor of the perturbation. 
	 */
	@Override
	public float getScale(Perturbation perturbation) {
		return 15F;
	}

	/**
	 * Method to retrieve the label of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new label of the perturbation. 
	 */
	@Override
	public String getLabel(Perturbation perturbation) {
		return null;
	}

	/**
	 * Method to retrieve the label font of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new label font of the perturbation. 
	 */
	@Override
	public Font getLabelFont(Perturbation perturbation) {
		return null;
	}

	/**
	 * Method to retrieve the label X offset of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new label X offset of the perturbation. 
	 */
	@Override
	public float getLabelXOffset(Perturbation perturbation) {
		return 0;
	}

	/**
	 * Method to retrieve the label Y offset of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new label Y offset of the perturbation. 
	 */
	@Override
	public float getLabelYOffset(Perturbation perturbation) {
		return 0;
	}

	/**
	 * Method to retrieve the label position of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new label position of the perturbation. 
	 */
	@Override
	public Position getLabelPosition(Perturbation perturbation) {
		return null;
	}

	/**
	 * Method to retrieve the label color of the perturbation.
	 * 
	 * @param perturbation: the perturbation to be represented;
	 * @return: the new label color of the perturbation. 
	 */
	@Override
	public Color getLabelColor(Perturbation perturbation) {
		return null;
	}
}
