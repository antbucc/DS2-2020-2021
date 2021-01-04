package ssbGossip;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


class FrontierTuple {
	private PublicKey id;
	private Integer index;

	public FrontierTuple(PublicKey id, Integer index) {
		this.id = id;
		this.index = index;
	}

	public PublicKey getId() {
		return id;
	}

	public Integer getIndex() {
		return index;
	}

}


public class Frontier implements Iterable<FrontierTuple> {
	List<FrontierTuple> frontier;

	public Frontier() {
		this.frontier = new ArrayList<>();
	}

	public void add(PublicKey id, Integer index) {
		add(new FrontierTuple(id, index));
	}

	public void add(FrontierTuple tuple) {
		this.frontier.add(tuple);
	}

	public boolean contains(PublicKey id) {
		for (FrontierTuple ft : frontier)
			if (ft.getId() == id)
				return true;
		return false;
	}

	public void remove(PublicKey id) {
		FrontierTuple tmp = null;
		
		for (FrontierTuple ft : frontier)
			if (ft.getId() == id)
				tmp = ft;
		
		if (tmp != null)
			frontier.remove(tmp);
	}

	@Override
	public Iterator<FrontierTuple> iterator() {
		return this.frontier.iterator();
	}
}
