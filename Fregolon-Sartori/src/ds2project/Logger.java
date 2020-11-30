package ds2project;

import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;

import repast.simphony.engine.environment.RunEnvironment;


public class Logger {
	static CSVWriter recPerturbation;
	static CSVWriter sentARQ;
	static CSVWriter totalPerturbations;

	static {
		try {
			recPerturbation = new CSVWriter(new FileWriter("output/ReceivedPerturbations.csv"));
			recPerturbation.writeNext(new String[]{"tickCount", "id", "type", "sender", "ref", "val", "latency"});
			sentARQ = new CSVWriter(new FileWriter("output/SentARQ.csv"));
			sentARQ.writeNext(new String[] {"tickCount", "sender", "src", "red"});
			totalPerturbations = new CSVWriter(new FileWriter("output/TotalPerturbations.csv"));
			totalPerturbations.writeNext(new String[]{"tickCount", "id", "type", "sender", "ref", "val", "latency"});
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void recordPerturbation(Relay1 receiver, Perturbation p, double tick) {
		String tickCount = String.valueOf(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		String id = String.valueOf(receiver.getId());
		String type = p instanceof ARQ ? "ARQ" : "Perturbation";
		String sender = String.valueOf(p.getSrc().getId());
		String ref = String.valueOf(p.getRef());
		String val = String.valueOf(p.getVal());
		String strLatency = String.valueOf(tick-p.generationTick);
		String[] data = new String[] {tickCount, id, type, sender, ref, val, strLatency};
		recPerturbation.writeNext(data);
	}

	public static void logSentARQ(Relay3 relay3, ARQ req) {
		String tickCount = String.valueOf(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		String sender = String.valueOf(relay3.getId());
		String src = String.valueOf(req.getSrc().getId());
		String ref = String.valueOf(req.getRef());
		String[] data = new String[] {tickCount, sender, src, ref};
		sentARQ.writeNext(data);
	}
	
	public static void totalPerturbations(Relay1 receiver, Perturbation p, double tick) {
		String tickCount = String.valueOf(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		String id = String.valueOf(receiver.getId());
		String type = p instanceof ARQ ? "ARQ" : "Perturbation";
		String sender = String.valueOf(p.getSrc().getId());
		String ref = String.valueOf(p.getRef());
		String val = String.valueOf(p.getVal());
		String strLatency = String.valueOf(tick-p.generationTick);
		String[] data = new String[] {tickCount, id, type, sender, ref, val, strLatency};
		totalPerturbations.writeNext(data);
	}
}
