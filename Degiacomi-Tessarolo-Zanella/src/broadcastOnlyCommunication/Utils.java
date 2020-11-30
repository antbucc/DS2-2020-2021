package broadcastOnlyCommunication;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Utils {
	public static double distanceBetweenPoints(GridPoint pt1, GridPoint pt2) {
		double xDiff = pt1.getX() - pt2.getX();
		double yDiff = pt1.getY() - pt2.getY();
		return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
	}

	public static int getNeededTimeToDeliver(double distance) {
		return (int) Math.ceil(distance) ^ 2;
	}

	public static List<Relay> getAllRelaysInGrid(Grid<Object> grid, Object actor) {
		return getAllActorsInGrid(grid, actor, Relay.class);
	}

	public static List<Station> getAllStationsInGrid(Grid<Object> grid, Object actor) {
		return getAllActorsInGrid(grid, actor, Station.class);
	}
	
	public static List<Perturbation> getAllPerturbationsInGrid(Grid<Object> grid, Object actor) {
		return getAllActorsInGrid(grid, actor, Perturbation.class);
	}

	public static <T> List<T> getAllActorsInGrid(Grid<Object> grid, Object actor, Class<T> clazz) {
		var pt = grid.getLocation(actor);
		var extentX = grid.getDimensions().getWidth() / 2;
		var extentY = grid.getDimensions().getHeight() / 2;
		var<T> nghCreator = new GridCellNgh<T>(grid, pt, clazz, extentX, extentY);
		return nghCreator.getNeighborhood(true).stream()
				.flatMap(cell -> StreamSupport.stream(cell.items().spliterator(), false)).collect(Collectors.toList());
	}
	
	public static Station instantiateCorrectStationVersion(ContinuousSpace<Object> space, Grid<Object> grid, String id) {
		Parameters params = RunEnvironment.getInstance().getParameters();
		String protocolVersion = params.getString("protocolVersion");
		
		if(protocolVersion.equals("Point-to-Point")) {
			return new Station(grid, id, true, false, false);
		} else if(protocolVersion.equals("PrivacyPreserving")) {
			return new Station(grid, id, true, true, false);
		} else if(protocolVersion.equals("GroupCommunication")) {
			return new Station(grid, id, false, false, true);
		} else {
			return new Station(grid, id, false, false, false);
		}
	}

	public static Relay instantiateCorrectRelayVersion(ContinuousSpace<Object> space, Grid<Object> grid, String id) {
		Parameters params = RunEnvironment.getInstance().getParameters();
		String protocolVersion = params.getString("protocolVersion");

		if (protocolVersion.equals("PerfectConditions")) {
			return new RelayI(space, grid, id);
		} else if (protocolVersion.equals("DynamicNetwork")) {
			return new RelayII(space, grid, id);
		} else if (protocolVersion.equals("RecoveringLoss") || protocolVersion.equals("Point-to-Point") || protocolVersion.equals("GroupCommunication")) {
			return new RelayIII(space, grid, id, false);
		} else if (protocolVersion.equals("PrivacyPreserving")) {
			return new RelayIII(space, grid, id, true);
		} else {
			System.err.println("Unsupported or not implemented protocol version!");
			return null;
		}
	}

	public static Perturbation createNewPerturbation(Grid<Object> grid, String src,
			int ref, String val, Object creator) {
		var p = new Perturbation(grid, src, ref, val);
		return startPropagatingPerturbation(grid, creator, p);
	}
	
	public static Perturbation createNewUnicastPerturbation(Grid<Object> grid, String src,
			int ref, String val, Object creator, String receiver) {
		var p = Perturbation.createUnicastPerturbation(grid, src, ref, val, receiver);
		return startPropagatingPerturbation(grid, creator, p);
	}
	
	public static Perturbation createNewTopicPerturbation(Grid<Object> grid, String src,
			int ref, String val, Object creator, String topic) {
		var p = Perturbation.createTopicPerturbation(grid, src, ref, val, topic);
		return startPropagatingPerturbation(grid, creator, p);
	}
	
	public static Perturbation forwardCopyOfPerturbation(Grid<Object> grid, Perturbation p, Object creator) {
		return startPropagatingPerturbation(grid, creator, p.copy());
	}
	
	private static Perturbation startPropagatingPerturbation(Grid<Object> grid, Object creator, Perturbation p) {
		var pt = grid.getLocation(creator);

		@SuppressWarnings("unchecked")
		var context = (Context<Perturbation>) ContextUtils.getContext(creator);
		p.setCenter(pt);
		context.add(p);
		grid.moveTo(p, (int) pt.getX(), (int) pt.getY());
		
		System.out.println("[UTILS] New perturbation: " + p + " at location " + pt);
		return p;
	}
	
	public static void removePerturbation(Perturbation p) {
		var context = ContextUtils.getContext(p);
		context.remove(p);
		System.out.println("[UTILS] Perturbation removed: " + p);
	}
	
	public static double getBiggestSizeOfGrid(Grid<Object> grid) {
		var dimensions = grid.getDimensions();
		return Math.max(dimensions.getWidth(), dimensions.getHeight());
	}
}
