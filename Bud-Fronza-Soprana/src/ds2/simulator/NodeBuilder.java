package ds2.simulator;

// Custom libraries
import ds2.utility.Options;
import ds2.visualization.DisplayManager;

// Repast libraries
import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;

public class NodeBuilder implements ContextBuilder<Object> {
	@Override
	public Context<Object> build(Context<Object> context) {
		// Set id of context
		context.setId("Ds2Assignment1");

		// Load all settings
		Options.load();

		// Create oracle and display and add them to the context (so that they can obtain the data)
		Oracle oracle = new Oracle();
		DisplayManager display = new DisplayManager();
				
		context.add(oracle);
		context.add(display);
		
		// Initialize oracle and display
		oracle.init(context);
		display.init(context);

		return context;
	}
}
