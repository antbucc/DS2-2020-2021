package ssb_broadcast.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import repast.simphony.util.collections.Pair;
import ssb_broadcast.Payload;

public final class CSVHelper {
	private static final String HEADER = "payload,delay";
	private static final String NEWLINE = "\n";
	private static final String COMMA = ",";
	private static Path BASE_PATH;
	
	public static void writeDelays(String filename, List<Pair<Payload, Integer>> delays) {
		File directory = new File(BASE_PATH.toString());
		if (!directory.exists()) {
	        directory.mkdirs();
	    }

		Path filePath = Paths.get(BASE_PATH.toString(), filename + ".csv");
	    File file = new File(filePath.toString());
	    try {
	    	PrintWriter writer = new PrintWriter(file);
	        StringBuilder stringbuilder = new StringBuilder();
			stringbuilder.append(HEADER);
			stringbuilder.append(NEWLINE);
			  
			for (Pair<Payload, Integer> delay : delays) {
				stringbuilder.append(delay.getFirst());
				stringbuilder.append(COMMA);
				stringbuilder.append(delay.getSecond());
				stringbuilder.append(NEWLINE);
			}
			
			writer.write(stringbuilder.toString());
			writer.close();
	    }
	    catch (IOException e){
	    	e.printStackTrace();
	    }
	}
	
	public static void createDirectory(long millis) {
		BASE_PATH = Paths.get("data", "" + millis);
	}
}
