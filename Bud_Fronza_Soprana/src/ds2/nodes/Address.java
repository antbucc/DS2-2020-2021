package ds2.nodes;

// Standard libraries
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

// Support libraries
import org.eclipse.jdt.annotation.Nullable;

import ds2.simulator.Oracle;
// Repast libraries
import repast.simphony.random.RandomHelper;

// NOTE: To be used as immutable
/**
 * This class represents the address of a {@link Machine}
 */
public class Address {
	public static int NEXT_ADDRESS = 0;
	
	private int address;
	
	public Address() {
		this.address = NEXT_ADDRESS;
		++NEXT_ADDRESS;
	}
	
	/**
	 * Get an address of a random alive machine
	 * @return an address of a random alive machine if there is one, null otherwise
	 */
	public static @Nullable Address getRandom() {
		// Skip if no address has ever been created
		if (Address.NEXT_ADDRESS == 0)
			return null;
		
		// Get all alive machines
		List<Machine> machines = Oracle.getInstance()
				.getAllMachines()
				.stream()
				.collect(Collectors.toList());
		
		// If none, skip
		if (machines.isEmpty())
			return null;
		
		// Get address of random alive machine
		return machines.get(RandomHelper.nextIntFromTo(0, machines.size()-1)).getAddress();
	}
	
	@Override
	public String toString() {
		return "A" + this.address;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof Address))
			return false;
		
		Address a = (Address) obj;
		
		return a.address == this.address;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.address);
	}
}
