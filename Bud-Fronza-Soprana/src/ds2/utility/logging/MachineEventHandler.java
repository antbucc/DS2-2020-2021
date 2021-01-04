package ds2.utility.logging;

// Standard libraries
import java.util.ArrayList;
import java.util.List;

// Custom libraries
import ds2.nodes.Address;
import ds2.nodes.Machine;
import ds2.utility.Event;
import ds2.utility.EventHandler;

/**
 * This class enables any class that extends it to use annotations to use a function to handle a specific type of event. 
 * This class represents layers which are supposed to be stacked (e.g.: physical, network, application) and provides functions to pass events to the upper layer(s) and the lower layer
 * @param <UP> Type of the layer above this
 * @param <DOWN> Type of the layer below this
 */
public class MachineEventHandler<UP extends MachineEventHandler<?, ?>, 
                                 DOWN extends MachineEventHandler<?, ?>> 
		extends EventHandler {
	ArrayList<UP> up = new ArrayList<>();
	DOWN down;
	
	/**
	 * Returns the address of the machine onto which this layer sits
	 * @return the address of the machine onto which this layers sits
	 */
	public Machine<?,?> getMachine() {
		MachineEventHandler<?,?> current = this;
		
		while (current != null) {
			if (current instanceof Machine) {
				return ((Machine<?, ?>)current);
			} else {
				current = current.down;
			}
		}
	
		throw new RuntimeException("Reached end of stack when looking for Machine but none was found");
	}
	
	/**
	 * Returns the address of the machine onto which this layer sits
	 * @return the address of the machine onto which this layers sits
	 */
	public Address getAddress() {
		return this.getMachine().getAddress();
	}
	
	/**
	 * Set what is the layer below
	 * @param down the layer below
	 */
	public void setDown(DOWN down) {
		this.down = down;
	}
	
	/**
	 * Set the layers on top of this layer
	 * @param ups the list of layers
	 */
	public void setUp(ArrayList<UP> ups) {
		this.up = ups;
	}
	
	/**
	 * Add a layer on top of this one
	 * @param up the layer to add
	 */
	public void addUp(UP up) {
		this.up.add(up);
	}

	/**
	 * Get the layer under this
	 * @return The layer under this
	 */
	public DOWN getDown() {
		return down;
	}
	
	/**
	 * Get the list of layers that sit on top of this
	 * @return The list of layers on top of this
	 */
	public List<UP> getUps() {
		return up;
	}

	/**
	 * Pass the specified event to the upper layer
	 * @param ev The event to pass to the upper layer
	 */
	protected void upcall(Event ev) {
		for (UP up2 : up) {
			up2.handle(ev);			
		}
	}

	/**
	 * Pass the specified event to the lower layer
	 * @param ev The event to pass to the lower layer
	 */
	protected void downcall(Event ev) {		
		this.down.handle(ev);
	}

	/*
	@SuppressWarnings(value = { "rawtypes", "unchecked" })
	public static void link(List<MachineEventHandler> handlers) {
		// TODO: Viva la fiducia
		for (int i=0; i<handlers.size(); ++i) {
			if (i-1 > 0) {
				handlers.get(i).setDown(handlers.get(i-1));
			}
			
			if (i+1 < handlers.size()) {
				handlers.get(i).setUp(handlers.get(i+1));
			}
		}
	}*/
	
	/**
	 * Constructor for a MachineEventHandler
	 * @param down The layer on which this layer sits on top
	 */
	public MachineEventHandler(DOWN down) {
		this.setDown(down);
	}
}
