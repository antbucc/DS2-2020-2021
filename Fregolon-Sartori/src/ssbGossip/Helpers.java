package ssbGossip;

import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVWriter;
import interfaces.Node;
import it.geosolutions.jaiext.stats.Min;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

//class which contains all the static helper classes
public class Helpers {
	// class which exposes parameters in order not to parse them every time
	static class ParamHelper {
		public static int nNodes;
		public static float meanGeneration;
		public static String simType;
		public static int nFollowed;
		public static int nBlocked;
		public static float followChangeProb;
		public static float blockChangeProb;
		public static int followChangeInterval;
		public static int followBlockInterval;
		public static float syncLambda;
		public static int insertInterval;
		public static int failInterval;
		public static float insertProb;
		public static float failureProb;
		public static double bandwidth;

		public static void load() {
			Parameters params = RunEnvironment.getInstance().getParameters();
			nNodes = params.getInteger("nNodes");
			meanGeneration = params.getFloat("meanGeneration");
			simType = params.getString("simtype");
			nFollowed = params.getInteger("nFollowed");
			nBlocked = params.getInteger("nBlocked");
			followChangeProb = params.getFloat("followChangeProb");
			blockChangeProb = params.getFloat("blockChangeProb");
			followChangeInterval = params.getInteger("followChangeInterval");
			followBlockInterval = params.getInteger("followBlockInterval");
			insertInterval = params.getInteger("insertInterval");
			failInterval = params.getInteger("failInterval");
			insertProb = params.getFloat("insertProb");
			failureProb = params.getFloat("failureProb");
			syncLambda = params.getFloat("syncLambda");
			bandwidth = params.getDouble("bandwidth");
		}
	}

	static class ContextHelper {
		public static Context<Object> context;
		public static Network<Object> graph;
		public static ContinuousSpace<Object> space;
		public static Network<Object> followGraph;
	}

	static class Logger {
		static CSVWriter recEvents;
		static double avgLatency;
		static int count;
		public static void load() throws IOException {
			count = 0;
			try {
				String filename="";
				if(ParamHelper.simType.equals("OpenGossip")) {
					filename+="op_";
				} else {
					filename+="ti_"+ParamHelper.nFollowed+"_";
				}
				filename+=ParamHelper.bandwidth+"_";
				filename+=ParamHelper.nNodes;
				
				recEvents = new CSVWriter(new FileWriter("output/"+filename+".csv"));
				recEvents.writeNext(new String[] {
					"tickCount", "receiver", "id", "index", "latency","avgLatency", "eventData"
				});
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		public static void recordEvent(Event e, Node n) {
			
			ISchedule s = RunEnvironment.getInstance().getCurrentSchedule();
			double latency;
			double followedFrom=-1;
			if(n instanceof TransitiveInterest) {
				TransitiveInterest ti = (TransitiveInterest) n;
				latency = s.getTickCount()-Math.max(ti.getFollowedFrom(e.getId()), Math.max(ti.getTickCreation(),
						e.getTickCreation()));
				followedFrom = ti.getFollowedFrom(e.getId());
			} else 
				latency = s.getTickCount()-Math.max(n.getTickCreation(), e.getTickCreation());
			
			if(count==0) {
				avgLatency = latency;
			} else {
				avgLatency = avgLatency*(count-1)/count + latency*1/count;
			}
			count++;
			String eventData = "-";
			if (e.getContent() instanceof InterestOperation)
				eventData = ((InterestOperation) e.getContent()).toString();
			
			recEvents.writeNext(
				new String[] { String.valueOf(s.getTickCount()),
						String.valueOf(n.getPublicKey().hashCode()),
						String.valueOf(e.getId().hashCode()),
						String.valueOf(e.getIndex()),
						String.valueOf(latency),
						String.valueOf(avgLatency),
						eventData});
		}

		public static void close() throws IOException {
			System.out.println("Closing log file");

			recEvents.close();
		}
	}

	static class NodeFinder {
		private static HashMap<PublicKey, Node> nodes = new HashMap<>();

		public static void addNode(Node n) {
			nodes.put(n.getPublicKey(), n);
		}

		public static Node getNode(PublicKey key) {
			return nodes.get(key);
		}
	}
}
