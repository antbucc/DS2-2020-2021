package gossipingAppendOnlyLogs;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

	public static String hash(String toHash) {
		try {
			var digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
			return byteArrayToHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("cannot compute SHA-256 hash", e);
		}
	}

	private static String byteArrayToHex(byte[] a) {
		var builder = new StringBuilder(a.length * 2);
		for (byte b : a) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}
}
