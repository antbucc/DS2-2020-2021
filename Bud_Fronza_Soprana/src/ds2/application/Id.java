package ds2.application;

/**
 * This class is use as some fake application data to make this data recognizable. It is only an increamental id for the data
 */
public class Id implements ApplicationData {
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
		return "Id" + id;
	}
}
