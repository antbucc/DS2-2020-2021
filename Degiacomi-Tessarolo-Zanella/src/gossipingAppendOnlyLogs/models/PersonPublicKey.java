package gossipingAppendOnlyLogs.models;

import gossipingAppendOnlyLogs.HashUtils;

import java.security.PublicKey;
import java.util.Base64;

public class PersonPublicKey {
	
    public final PublicKey key;

    public PersonPublicKey(PublicKey key) {
        this.key = key;
    }

    public String base64Encoded() {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public String hash() {
        return HashUtils.hash(base64Encoded());
    }

	@Override
	public String toString() {
		return hash().substring(0, 7);
	}
}
