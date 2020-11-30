package bcastonly;

/**
 * Representation of a perturbation message
 * 
 * @author bortolan cosimo
 */
public class Perturbation {
	private int src;
	private int ref;
	private Object val;

	public Perturbation(int src, int ref, Object val) {
		this.src = src;
		this.ref = ref;
		this.val = val;
	}

	public int getSrc() {
		return src;
	}

	public int getRef() {
		return ref;
	}

	public Object getVal() {
		return val;
	}

}
