package ds2.protocol.messages;

// Standard libraries
import java.util.ArrayList;

// Custom libraries
import ds2.nodes.Address;

public class ARQHistoryReply extends Perturbation{

	ArrayList<Soliton> perSrcHistory;
	
	public ARQHistoryReply(Address source, ArrayList<Soliton> perSrcHistory) {
		super(source);
		this.perSrcHistory = perSrcHistory;
	}

	public ArrayList<Soliton> getPerSrcHistory() {
		return perSrcHistory;
	}

	public void setPerSrcHistory(ArrayList<Soliton> perSrcHistory) {
		this.perSrcHistory = perSrcHistory;
	}
	
	@Override
	public String toString() {
		return super.toString() + " history"; 
	}
	

}
