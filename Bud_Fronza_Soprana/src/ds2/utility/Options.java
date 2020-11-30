package ds2.utility;

// Standard libraries
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Repast libraries
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public class Options {
	public static String OUTPUT_PATH;
	
	//--- Simulation stuff
	public static double MEAN_CREATE;
	public static double VAR_CREATE;

	public static double MEAN_KILL;
	public static double VAR_KILL;

	public static double MEAN_BROADCAST_EVENT;
	public static double VAR_BROADCAST_EVENT;
	
	public static double MEAN_MULTICAST_EVENT;
	public static double VAR_MULTICAST_EVENT;

	public static double MEAN_UNICAST_EVENT;
	public static double VAR_UNICAST_EVENT;
	
	public static int    INITIAL_NODE_SIZE;
	public static int    MAX_NODE_SIZE;
	public static int    TOPIC_NUM;
	
	public static double STOP_TIME;

	//--- Network stuff
	public static double PROPAGATION_SPEED;
	public static double TRANSMISSION_TIME;
	
	public static double PROCESSING_DELAY;
	public static double MAX_RANDOM_DELAY;
	
	public static double NORMAL_DIST;
	public static double CUT_OFF;
	
	public static double SNR;
	public static double VAR_NOISE;
	
	public static int PACKET_SIZE;
	public static boolean DEBUG_COLLISIONS;
	
	// Protocol stuff
	public static double ARQ_INTERVAL;
	public static boolean ARQnull_OPT;
	public static boolean HANDLE_ARQ_OPT;
	public static int OPTIMIZATION; // 0 no, 1 yes
	
	//--- Visualization
	public static double DISPLAY_SIZE;
	

	public static void load() {
		Parameters params = RunEnvironment.getInstance().getParameters();
		OPTIMIZATION = 1;
		//--- Simulation options
		OUTPUT_PATH          = params.getString("OUTPUT_PATH");
		
		ARQnull_OPT			 = params.getBoolean("ARQnull_OPT");
		
		HANDLE_ARQ_OPT		 = params.getBoolean("HANDLE_ARQ_OPT");
		
		MEAN_CREATE          = params.getDouble("MEAN_CREATE");
		VAR_CREATE           = params.getDouble("VAR_CREATE");

		MEAN_KILL            = params.getDouble("MEAN_KILL");
		VAR_KILL             = params.getDouble("VAR_KILL");

		MEAN_BROADCAST_EVENT = params.getDouble("MEAN_BROADCAST_EVENT");
		VAR_BROADCAST_EVENT  = params.getDouble("VAR_BROADCAST_EVENT");

		MEAN_MULTICAST_EVENT = params.getDouble("MEAN_MULTICAST_EVENT");
		VAR_MULTICAST_EVENT  = params.getDouble("VAR_MULTICAST_EVENT");

		MEAN_UNICAST_EVENT   = params.getDouble("MEAN_UNICAST_EVENT");
		VAR_UNICAST_EVENT    = params.getDouble("VAR_UNICAST_EVENT");

		INITIAL_NODE_SIZE    = params.getInteger("INITIAL_NODE_SIZE");
		MAX_NODE_SIZE        = params.getInteger("MAX_NODE_SIZE");
		TOPIC_NUM            = params.getInteger("TOPIC_NUM");

		STOP_TIME            = params.getDouble("STOP_TIME");

		//--- Network options
		PROPAGATION_SPEED    = params.getDouble("PROPAGATION_SPEED");
		// Get the actual transmission time instead of the bitrate
		TRANSMISSION_TIME    = ((double)params.getInteger("PACKET_SIZE"))/(1000. * params.getDouble("BITRATE")/8.);

		PROCESSING_DELAY     = params.getDouble("PROCESSING_DELAY");
		MAX_RANDOM_DELAY     = params.getDouble("MAX_RANDOM_DELAY");
		
		NORMAL_DIST          = params.getDouble("NORMAL_DIST");
		CUT_OFF              = params.getDouble("CUT_OFF");

		SNR                  = params.getDouble("SNR");
		VAR_NOISE            = params.getDouble("VAR_NOISE");

		PACKET_SIZE          = params.getInteger("PACKET_SIZE");
		DEBUG_COLLISIONS     = params.getBoolean("DEBUG_COLLISIONS");

		//--- Protocol options
		ARQ_INTERVAL         = params.getDouble("ARQ_INTERVAL");

		//--- Visualization options
		DISPLAY_SIZE         = params.getDouble("DISPLAY_SIZE");
	}
	
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
