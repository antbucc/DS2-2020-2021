package gossipingAppendOnlyLogs.models;

public class FrontierItem {
	public final PersonPublicKey id;
	public final int last;

	public FrontierItem(PersonPublicKey id, int last) {
		this.id = id;
		this.last = last;
	}
}
