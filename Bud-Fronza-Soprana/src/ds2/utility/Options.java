package ds2.utility;

// Standard libraries
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Repast libraries
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

/**
 * This class represents the options of the simulation. The options will be automatically reloaded on change
 */
public class Options {
	public static String OUTPUT_PATH;
	
	public static boolean CONSOLE_OUTPUT = true;
	
	public static enum ProtocolType{
		OPEN_GOSSIP,
		TRANSITIVE_INTEREST
	};
	
	//--- Simulation stuff
	public static double MEAN_CREATE;
	public static double VAR_CREATE;

	public static double MEAN_KILL;
	public static double VAR_KILL;

	public static double MEAN_EVENT;
	public static double VAR_EVENT;
	
	public static double MEAN_FOLLOW;
	public static double VAR_FOLLOW;
	
	public static double MEAN_UNFOLLOW;
	public static double VAR_UNFOLLOW;
	
	public static double MEAN_BLOCK;
	public static double VAR_BLOCK;
	
	public static double MEAN_UNBLOCK;
	public static double VAR_UNBLOCK;
	
	public static double MEAN_RECREATE_DELAY;
	public static double VAR_RECREATE_DELAY;
	
	public static double MEAN_GOOFFLINE;
	public static double VAR_GOOFFLINE;

	public static int    INITIAL_NODE_SIZE;
	public static int    MAX_NODE_SIZE;
	
	public static double ALPHA_IDENTITIES_PER_NODE;
	public static double LAMBDA_IDENTITIES_PER_NODE;
	
	public static int MIN_FOLLOWED_PER_NODE;
	public static int MAX_FOLLOWED_PER_NODE;
	
	public static double STOP_TIME;

	//--- Network stuff
	public static double PROPAGATION_SPEED;
	public static double TRANSMISSION_TIME;
	
	public static double PROCESSING_DELAY;
	
	// Protocol stuff
	public static ProtocolType PROTOCOL_TYPE;
	public static double UPDATE_INTERVAL;
		
	//--- Visualization
	public static double DISPLAY_SIZE;
	
	/**
	 * This function loads the options
	 */
	public static void load() {
		Parameters params = RunEnvironment.getInstance().getParameters();
		//--- Simulation options
		OUTPUT_PATH          = params.getString("OUTPUT_PATH");
		CONSOLE_OUTPUT       = params.getBoolean("CONSOLE_OUTPUT");
				
		MEAN_CREATE          = params.getDouble("MEAN_CREATE");
		VAR_CREATE           = params.getDouble("VAR_CREATE");

		MEAN_KILL            = params.getDouble("MEAN_KILL");
		VAR_KILL             = params.getDouble("VAR_KILL");

		MEAN_EVENT = params.getDouble("MEAN_EVENT");
		VAR_EVENT  = params.getDouble("VAR_EVENT");
		
		MEAN_FOLLOW = params.getDouble("MEAN_FOLLOW");
		VAR_FOLLOW = params.getDouble("VAR_FOLLOW");
		
		MEAN_UNFOLLOW = params.getDouble("MEAN_UNFOLLOW") ;
		VAR_UNFOLLOW = params.getDouble("VAR_UNFOLLOW");
		
		MEAN_BLOCK = params.getDouble("MEAN_BLOCK");
		VAR_BLOCK = params.getDouble("VAR_BLOCK");
		
		MEAN_UNBLOCK = params.getDouble("MEAN_UNBLOCK");
		VAR_UNBLOCK = params.getDouble("VAR_UNBLOCK");
		
		MEAN_GOOFFLINE = params.getDouble("MEAN_GOOFFLINE");
		VAR_GOOFFLINE = params.getDouble("VAR_GOOFFLINE");
		
		MEAN_RECREATE_DELAY = params.getDouble("MEAN_RECREATE_DELAY");
		VAR_RECREATE_DELAY = params.getDouble("VAR_RECREATE_DELAY");

		INITIAL_NODE_SIZE    = params.getInteger("INITIAL_NODE_SIZE");
		MAX_NODE_SIZE        = params.getInteger("MAX_NODE_SIZE");
		
		/* Gamma distribution
		 * mean = alpha/lambda
		 * var = alpha/(lambda^2)
		 * 
		 * we remove 1 from the mean as we later transpose it to be always at least 1
		 */
		LAMBDA_IDENTITIES_PER_NODE = (params.getDouble("MEAN_IDENTITIES_PER_NODE")-1)/params.getDouble("VAR_IDENTITIES_PER_NODE");
		ALPHA_IDENTITIES_PER_NODE = (params.getDouble("MEAN_IDENTITIES_PER_NODE")-1)*LAMBDA_IDENTITIES_PER_NODE;
		
		MIN_FOLLOWED_PER_NODE = params.getInteger("MIN_FOLLOWED_PER_NODE");
		MAX_FOLLOWED_PER_NODE = params.getInteger("MAX_FOLLOWED_PER_NODE");
		
		STOP_TIME            = params.getDouble("STOP_TIME");

		//--- Network options
		PROPAGATION_SPEED    = params.getDouble("PROPAGATION_SPEED");

		PROCESSING_DELAY     = params.getDouble("PROCESSING_DELAY");

		//--- Protocol options
		UPDATE_INTERVAL         = params.getDouble("UPDATE_INTERVAL");
		
		switch (params.getString("PROTOCOL_TYPE")) {
			case "OPEN_GOSSIP": PROTOCOL_TYPE = ProtocolType.OPEN_GOSSIP; break;
			case "TRANSITIVE_INTEREST": PROTOCOL_TYPE = ProtocolType.TRANSITIVE_INTEREST; break;
		}
		
		//--- Visualization options
		DISPLAY_SIZE         = params.getDouble("DISPLAY_SIZE");
	}
	
	/**
	 * This function automatically adds a listener which will reload the options on change
	 */
	public static void addListener() {
		Parameters params = RunEnvironment.getInstance().getParameters();

		params.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Options.load();
			}
		});
	}
}
