package gossipingAppendOnlyLogs.models;

public class PersonKeys {
    public final PersonPublicKey publicKey;
    public final PersonPrivateKey privateKey;

    public PersonKeys(PersonPublicKey publicKey, PersonPrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
}
