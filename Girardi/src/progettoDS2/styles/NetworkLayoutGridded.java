package progettoDS2.styles;

import javax.vecmath.Point3f;

import progettoDS2.Relay;
import repast.simphony.space.projection.Projection;
import repast.simphony.visualization.Box;
import repast.simphony.visualization.Layout;
import repast.simphony.visualization.VisualizationProperties;

public class NetworkLayoutGridded implements Layout {

  @Override
  public void update() { }

  @Override
  public void setProjection(Projection projection) { }

  @Override
  public float[] getLocation(Object obj) {
    if(obj instanceof Relay) {
      return new float[] { 
        (float)((Relay)obj).getX() * 30, 
        (float)((Relay)obj).getY() * 30
      };
    }
    return null;
  }

  @Override
  public void setLayoutProperties(VisualizationProperties props) { }

  @Override
  public VisualizationProperties getLayoutProperties() {
    return null;
  }

  @Override
  public String getName() {
    return "NetworkLayoutGridder";
  }

  @Override
  public Box getBoundingBox() {
    return new Box(new Point3f(0, 0, 0), new Point3f(0, 0, 0));
  }

}
