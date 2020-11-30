package ds2.utility;

import java.io.BufferedOutputStream;
//Standard libraries
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.nodes.Address;
import ds2.simulator.Oracle;

// Repast libraries
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public class Logger {
	
	static class BaseLogger {
		@NonNull FileOutputStream fos;
		@NonNull ZipOutputStream zipOut;

		public BaseLogger(String outputPath, @NonNull String name) {
			try {
				this.fos = new FileOutputStream(outputPath + name + ".zip");				
			} catch (FileNotFoundException e) {
				System.err.println("[Logger]: Impossible to create file");
				e.printStackTrace();
				System.exit(1);
			}
			
			this.zipOut = new ZipOutputStream(new BufferedOutputStream(fos));
			try {
				this.zipOut.putNextEntry(new ZipEntry(name + ".log"));
			} catch (IOException e) {
				System.err.println("[Logger]: Impossible to create a zip entry");
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		public void write(String str) throws IOException {
			byte bytes[] = str.getBytes();
			zipOut.write(bytes, 0, bytes.length);
		}

		public void flush() throws IOException {
			this.zipOut.flush();
			this.fos.flush();
		}
		
		public void close() throws IOException {
			this.zipOut.close();
			this.fos.close();		
		}
	}
	
	String outputPath;

	@NonNull HashMap<Address, BaseLogger> machineLogs = new HashMap<>();
	BaseLogger oracleLogger;
	
	private BaseLogger getMachineLogger(Address addr) {
		@Nullable BaseLogger ml = this.machineLogs.get(addr);
		if (ml == null) {
			ml = new BaseLogger(outputPath, addr.toString());
			
			this.machineLogs.put(addr, ml);
		}
		
		return ml;
	}
	
	private void machinePrint(Address addr, String tag, String str, double timestamp, PrintStream dup) {
		BaseLogger ml = this.getMachineLogger(addr);
		
		String out = "[ " + tag + " " + addr + " " + timestamp + " ]: " + str; 
		
		dup.println(out);
		try {
			ml.write(out + "\n");			
		} catch (IOException e) {
			System.err.println("[Logger]: Impossible to write file");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void machineLog(Address addr, String tag, double timestamp, String str) {
		this.machinePrint(addr, tag, str, timestamp, System.out);
	}
	
	public void machineErr(Address addr, String tag, double timestamp, String str) {
		this.machinePrint(addr, tag, str, timestamp, System.err);
	}
	
	private void oraclePrint(String tag, String str, double timestamp, PrintStream dup) {		
		String out = "[ " + tag + " " + timestamp + " ]: " + str; 
		
		dup.println(out);
		try {
			this.oracleLogger.write(out + "\n");			
		} catch (IOException e) {
			System.err.println("[Logger]: Impossible to write file");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void oracleLog(String tag, double timestamp, String str) {
		this.oraclePrint(tag, str, timestamp, System.out);
	}
	
	public void oracleErr(String tag, double timestamp, String str) {
		this.oraclePrint(tag, str, timestamp, System.err);
	}


	public void flush() throws IOException {
		for (BaseLogger ml : this.machineLogs.values()) {
			ml.flush();
		}
		
		oracleLogger.flush();
	}
	
	public void close() throws IOException {
		for (BaseLogger ml : this.machineLogs.values()) {
			ml.close();
		}
		
		oracleLogger.close();
	}
	
	public Logger(String outputPath) {
		this.outputPath = outputPath;
		
		File baseFolder = new File(outputPath);
		baseFolder.mkdirs();
		
		try {
			FileWriter initialOptions = new FileWriter(new File(outputPath + "state.log"));
			Parameters params = RunEnvironment.getInstance().getParameters();
			
			params.getSchema().parameterNames().forEach(name -> {
				try {
					// Do not write if a network is directed or not <network>directed
					if (name.endsWith("directed"))
						return;
				
					initialOptions.write(name  + ": " + params.getValueAsString(name) + "\n");
				} catch (IOException e) { /* Ignore */ }
			});
			
			initialOptions.flush();
			initialOptions.close();
		} catch (IOException e) {
			System.err.println("[Logger]: Cannot write options");
			e.printStackTrace();
			System.exit(1);
		}
		
		this.oracleLogger = new BaseLogger(outputPath, "Oracle");
	}
	
	public static void init() {
		// Get current time in ISO8601
		String outputPath = new String(Options.OUTPUT_PATH);
		String now = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.now()).replace(".", "-").replace(":", "-");

		// Create path and folder
		if (!outputPath.endsWith(File.separator))
			outputPath += java.io.File.separator;
		
		outputPath += now + java.io.File.separator;
		
		Oracle.getInstance().logger = new Logger(outputPath);
	}
}
