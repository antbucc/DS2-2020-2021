package ds2.protocol.messages;

// Custom libraries
import ds2.nodes.Address;

public class Soliton extends Perturbation {

	//ProtocolData, either Broadcast/Unicast/Multicast message 
	private ProtocolMsg data;  
	private int seqn;   //unique ref
	

	public Soliton(Address source, ProtocolMsg data, int seqn) {
		super(source);
		this.data = data;
		this.seqn = seqn;
	
	}


	public  ProtocolMsg getData() {
		return data;
	}


	public void setData( ProtocolMsg data) {
		this.data = data;
	}


	public int getSeqn() {
		return seqn;
	}

	public void setSeqn(int seqn) {
		this.seqn = seqn;
	}
	
	@Override
	public String toString() {
		return super.toString() + ", val:" + data.toString() + ", ref:" + seqn;
	}
	
	
	
}
