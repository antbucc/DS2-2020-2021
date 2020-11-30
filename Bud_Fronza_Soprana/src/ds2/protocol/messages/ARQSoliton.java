package ds2.protocol.messages;

// Custom libraries
import ds2.nodes.Address;

public class ARQSoliton extends Perturbation{

	/* The meaning of source and seqn are different than in a normal Soliton:
	* -- Source is the source for which an update is requested
	* -- seqn holds information about the next expected reference for that source, 
	* whereas in Soliton it holds the sequence number of the current message 
	 */
	int seqn;

	public ARQSoliton(Address source, int seqn) {
		super(source);
		this.seqn = seqn;
		
	}
	
	public int getSeqn() {
		return seqn;
	}

	public void setSeqn(int seqn) {
		this.seqn = seqn;
	}
	
	public String getStringSource() {
		if (this.source != null)
			return this.source.toString();
		else return "null";
	}
	
	@Override
	public String toString() {
		
		if (this.source != null)
			return "ARQ:" + this.source.toString() + ", ref:" + seqn;
		else return "ARQ:null" + ", ref:" + seqn;
	}
	
	@Override 
	public boolean equals(Object other) {
		if (this == other)
			return true;
		
		if (other == null)
			return false;
		
		if (!(other instanceof ARQSoliton))
			return false;
		
		ARQSoliton arq = (ARQSoliton) other;
		
		return this.source.equals(arq.source) && this.seqn == arq.seqn;
		
	}
}
