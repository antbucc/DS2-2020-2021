package ds2.utility.logging;

//Standard libraries
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

// Custom libraries
import ds2.nodes.Address;
import ds2.simulator.Oracle;
import ds2.utility.Options;

// Repast libraries
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

/**
 * This class manages initializing and providing logging
 */
public class Logger {
	
	public static String formatPort(java.security.PublicKey pk) {
		if (pk instanceof java.security.interfaces.RSAPublicKey) {
			return Base64.getEncoder().encodeToString(((java.security.interfaces.RSAPublicKey)pk).getModulus().toByteArray());
		} else {
			throw new IllegalArgumentException("Only RSA keys are supported");
		}
	}
	
	/**
	 * This represents a base logger
	 */
	public static class BaseLogger {
		@NonNull FileOutputStream fos;
		@NonNull ZipOutputStream zipOut;

		/**
		 * Constructor for BaseLogger
		 * @param outputPath path of the folder the log file will be created
		 * @param name name of the log file
		 */
		protected BaseLogger(@NonNull String outputPath, @NonNull String name) {
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
		
		/**
		 * Print tags and message to the logger
		 * @param dup Stream in which the data is duplicated (usually stdout or stderr). Can be null
		 * @param msg Message to write
		 * @param tags Tags to write inside the [ ... ]:
		 */
		public void print(@Nullable PrintStream dup, @NonNull String msg, @NonNull String... tags) {
			String out = "[ " + String.join(" ", tags) + " ]: " + msg + "\n";
			
			if (Options.CONSOLE_OUTPUT && dup != null) {
				dup.print(out);
			}
			
			try {
				this.write(out);			
			} catch (IOException e) {
				System.err.println("[Logger]: Impossible to write file");
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		/**
		 * Writes a raw string to the logger
		 * @param str The string to write
		 * @throws IOException throws if for any reason it's impossible to write to the log file
		 */
		private void write(@NonNull String str) throws IOException {
			byte bytes[] = str.getBytes();
			zipOut.write(bytes, 0, bytes.length);
		}

		/**
		 * Flush the log file
		 * @throws IOException throws if for any reason the log file can't be flushed
		 */
		public void flush() throws IOException {
			this.zipOut.flush();
			this.fos.flush();
		}
		
		/**
		 * Close the log file
		 * @throws IOException throws if for any reason the log file can't be closed
		 */
		public void close() throws IOException {
			this.zipOut.close();
			this.fos.close();		
		}
	}
	
	/**
	 * This represents the logger used by Oracle
	 */
	public static class OracleLogger extends BaseLogger {
		/**
		 * Constructor for OracleLogger
		 * @param outputPath path of the folder in which the log will be saved
		 */
		public OracleLogger(@NonNull String outputPath) {
			super(outputPath, "Oracle");
		}
	}
	
	String outputPath;

	@NonNull HashMap<Address, BaseLogger> machineLogs = new HashMap<>();
	BaseLogger oracleLogger;
	
	/**
	 * Returns the logger of a machine
	 * @param addr The address of machine
	 * @return The logger of the machine. Always not null
	 */
	public @NonNull BaseLogger getMachineLogger(Address addr) {
		@Nullable BaseLogger ml = this.machineLogs.get(addr);
		if (ml == null) {
			ml = new BaseLogger(outputPath, addr.toString());
			
			this.machineLogs.put(addr, ml);
		}
		
		return ml;
	}
	
	/**
	 * Returns the logger for the Oracle
	 * @return the logger for the Oracle
	 */
	public BaseLogger getOracleLogger() {
		return this.oracleLogger;
	}

	/**
	 * Flush all the loggers
	 * @throws IOException An exception is thrown if for any reason the log files can't be flushed
	 */
	public void flush() throws IOException {
		for (BaseLogger ml : this.machineLogs.values()) {
			ml.flush();
		}
		
		oracleLogger.flush();
	}

	/**
	 * Close all the log files
 	 * @throws IOException An exception is thrown if for any reason the log files can't be closed
	 */
	public void close() throws IOException {
		for (BaseLogger ml : this.machineLogs.values()) {
			ml.close();
		}
		
		oracleLogger.close();
	}

	/**
	 * Constructor for Logger
	 * @param outputPath The path of the folder in which we want to create the log files
	 */
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
		
		this.oracleLogger = new OracleLogger(outputPath);
	}
	
	/**
	 * Initialize the logging utilities
	 */
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
