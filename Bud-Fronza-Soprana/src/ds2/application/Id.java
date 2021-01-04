package ds2.application;

/**
 * This class is used as some fake application data to make this data recognizable. It is only an incremental id for the data
 */
public class Id {
	public static int NEXT_ID;
	
	int id;

	/**
	 * Construct for Id. It automatically generates an unique Id
	 */
	public Id() {
		this.id = NEXT_ID;
		NEXT_ID++;
	}
	
	public String toString() {
		return ""+id;
	}
}
