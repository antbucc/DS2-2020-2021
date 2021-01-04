package gossipingAppendOnlyLogs.actors;

import gossipingAppendOnlyLogs.RepastUtils;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LAN {
	public final String ID;
	public final double maximumDistance = 5;

	public LAN(String id) {
		ID = id;
	}

	public Set<Person> getConnectedPeople() {
		return RepastUtils.getAllPeopleInGrid()
				.stream()
				.filter(p -> {
					var lan = p.getConnectedLAN();
					if (lan.isEmpty()) {
						return false;
					}
					return lan.get().equals(this);
				})
				.collect(Collectors.toSet());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LAN lan = (LAN) o;
		return ID.equals(lan.ID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ID);
	}
}
