package gossipingAppendOnlyLogs.models;

import java.security.PrivateKey;

public class PersonPrivateKey {

    public final PrivateKey key;

    public PersonPrivateKey(PrivateKey key) {
        this.key = key;
    }
}
