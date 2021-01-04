package ssb_broadcast;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import ssb_broadcast.Payload.PayloadType;


/**
 * Defines a Perturbation that carries a set of Payloads
 * 
 * @author Alessandro Conti and Francesco Tonini
 *
 */
public class Perturbation {	
	// Grid definition of the 2D space
	private final Grid<Object> grid;
	
	// The payload  nested in this Perturbation
	private final Payload payload;
	
	// Speed of this perturbation
	private final double valueSpeed;
	
	// List of Observer already sensed by this Perturbation
	private final List<Observer> contacted;
	
	// TTL of this Perturbation
	private final int ttl;
	
	// Radius of this perturbation
	private final int radius;
	
	// Current TTL, will be decreased at each tick
	private int currentTTL = 0;
	
	// Initial size of this perturbation
	private double value = Constants.CELL_SIZE;
	
	public Perturbation(Grid<Object> grid, Payload payload) {
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		this.grid = grid;
		this.payload = payload;
		this.contacted = new ArrayList<>();
		this.valueSpeed = params.getDouble("perturbation_speed");
		this.radius = params.getInteger("perturbation_radius");
		this.ttl = (int)((this.radius * 2 - 1) * Constants.CELL_SIZE / this.valueSpeed);
	}

	public double getValue() {
		// System.out.format("value: %f \n", value);
		
		return this.value;
	}
	
	public int isDiscovery() {
		return this.payload.getType() == PayloadType.DISCOVERY ? 1 : 0;
	}
	
	public int isRequest() {
		return this.payload.getType() == PayloadType.REQUEST ? 1 : 0;
	}
	
	public int isReply() {
		return this.payload.getType() == PayloadType.REPLY ? 1 : 0;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void expand() {
		// Check if Perturbation has still some TTL
		if (this.currentTTL >= this.ttl) {
			// Get the context of the simulation
			@SuppressWarnings("unchecked")
			Context<Object> context = ContextUtils.getContext(this);
			
			// Remove the perturbation
			context.remove(this);
			
			return;
		}
		
		this.currentTTL++;
		this.value += this.valueSpeed;
	
		// System.out.format("ttl: %d scale: %f \n", ttl, scale);
	
		this.senseNeighbors();
	}
	
	private void senseNeighbors() {
		// Get current position of this perturbation
		GridPoint point = this.grid.getLocation(this);
		
		// Get the extent
		int extent = (int)(this.getValue() / (Constants.CELL_SIZE * 2));
		
		GridCellNgh<Observer> nghCreator = new GridCellNgh<Observer>(this.grid, point, Observer.class, extent, extent);
		List<GridCell<Observer>> neighbors = nghCreator.getNeighborhood(false);
		
		// Iterate through all neighbors and find Observers to sense
		for (GridCell<Observer> cell: neighbors) {
			List<Object> objs = new ArrayList<>();
			this.grid.getObjectsAt(cell.getPoint().getX(), cell.getPoint().getY()).forEach(objs::add);

			for (Object obj: objs) {
				if (obj instanceof Observer && !this.contacted.contains(obj)) {
					Observer observer = ((Observer)obj);
					
					// Add the current observer to the contacted ones
					this.contacted.add(observer);
					
					// Send each Payload to the observer
					observer.sense(payload, this.currentTTL);
				}
			}
		}
	}
}
