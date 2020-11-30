package ds2project;


public class RelayedPertubation {
	private Perturbation p;
	private Relay1 n;

	public RelayedPertubation(Perturbation p, Relay1 n) {
		this.p = p;
		this.n = n;
	}

	public Perturbation getP() {
		return p;
	}

	public Relay1 getN() {
		return n;
	}
}
