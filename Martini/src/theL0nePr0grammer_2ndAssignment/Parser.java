package theL0nePr0grammer_2ndAssignment;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import theL0nePr0grammer_2ndAssignment.Relays.*;

public class Parser {
	private JSONObject json;
	private int index;
	
	public Parser(String path) {
		index = 0;
		try {
			json = (JSONObject) new JSONParser().parse(new FileReader(path));
		} catch (Exception e) {
			System.out.println("Error opening file");
		} 
	}
	
	class Dimensions {
		private int xdim;
		private int ydim;
		
		public Dimensions(int x, int y) {
			this.xdim = x;
			this.ydim = y;
		}
		
		public int x() {
			return this.xdim;
		}
		
		public int y() {
			return this.ydim;
		}
	}
	
	public Dimensions getDimensions() {
		return new Dimensions(
			(int) (long) ((JSONObject) this.json.get("Dimensions")).get("X"),
			(int) (long) ((JSONObject) this.json.get("Dimensions")).get("Y"));
	}
	
	public int getDuration() {
		return (int) (long) this.json.get("Duration");
	}
	
	public String getNetwork() {
		return (String) this.json.get("Network");
	}
	
	public List<Relay> getRelaysList() {
		ArrayList<Relay> toReturn = new ArrayList<Relay>();
		
		if (json == null)
			return null;
		
		JSONArray fixrelays = (JSONArray) json.get("FixedRelays");
		if (fixrelays != null) {
			for (Object item : fixrelays) {
				JSONObject conv =  (JSONObject) item;
				Relay generated = null;
				
				if (((String) this.json.get("Network")).equals("OpenRelay")) {
					generated = new OpenRelay(
							index++, 
							(double) conv.get("X"), 
							(double) conv.get("Y"),  
							(double) conv.get("Latency"),
							(double) conv.get("Jitter"),
							(double) conv.get("PerturbationProb"),
							(double) conv.get("SpawnProb"),
							(double) conv.get("LeaveProb"),
							(double) conv.get("LossProb"));
				} else if (((String) this.json.get("Network")).equals("TransientRelay")) {
					generated = new TransientRelay(
							index++, 
							(double) conv.get("X"), 
							(double) conv.get("Y"),  
							(double) conv.get("Latency"),
							(double) conv.get("Jitter"),
							(double) conv.get("PerturbationProb"),
							(double) conv.get("SpawnProb"),
							(double) conv.get("LeaveProb"),
							(double) conv.get("LossProb"));
				}
				toReturn.add(generated);
			}
		}
		
		JSONArray randrelays = (JSONArray) json.get("RandomRelays");
		if (randrelays != null) {
			for (Object item : randrelays) {
				JSONObject conv =  (JSONObject) item;
				Relay generated = null;
				
				int count = (int) (long) conv.get("Count");
	
				for (int i = 0; i < count; i++) {
					if (((String) this.json.get("Network")).equals("OpenRelay")) {
						generated = new OpenRelay(
								index++, 
								-1, 
								-1,  
								(double) conv.get("Latency"),
								(double) conv.get("Jitter"),
								(double) conv.get("PerturbationProb"),
								(double) conv.get("SpawnProb"),
								(double) conv.get("LeaveProb"),
								(double) conv.get("LossProb"));
					} else if (((String) this.json.get("Network")).equals("TransientRelay")) {
						generated = new TransientRelay(
								index++, 
								-1, 
								-1,  
								(double) conv.get("Latency"),
								(double) conv.get("Jitter"),
								(double) conv.get("PerturbationProb"),
								(double) conv.get("SpawnProb"),
								(double) conv.get("LeaveProb"),
								(double) conv.get("LossProb"));
					}
					toReturn.add(generated);
				}
			}
		}

		return toReturn;
	}
}
