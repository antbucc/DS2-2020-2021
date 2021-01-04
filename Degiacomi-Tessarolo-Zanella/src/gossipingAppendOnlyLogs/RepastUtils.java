package gossipingAppendOnlyLogs;

import gossipingAppendOnlyLogs.actors.LAN;
import gossipingAppendOnlyLogs.actors.Person;
import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepastUtils {

    public static Grid<Object> grid;

    public static Context<Object> context;

    public static ContinuousSpace<Object> space;

	private static final List<Person> people = new ArrayList<>();
	private static final List<LAN> LANs = new ArrayList<>();

	public static void init (Grid<Object> grid, Context<Object> context, ContinuousSpace<Object> space) {
		RepastUtils.grid = grid;
		RepastUtils.context = context;
		RepastUtils.space = space;
		people.clear();
		LANs.clear();
	}

	public static void addLAN(LAN lan) {
		context.add(lan);
		LANs.add(lan);
	}

	public static void addPerson(Person person) {
		context.add(person);
		people.add(person);
	}

    public static List<LAN> getAllLANsInGrid() {
		return Collections.unmodifiableList(LANs);
    }

    public static List<Person> getAllPeopleInGrid() {
		return Collections.unmodifiableList(people);
    }

    public static double getDistance(Person person, LAN lan) {
        return space.getDistance(
                space.getLocation(person),
                space.getLocation(lan)
        );
    }

    public static void moveTo(Object obj, double x, double y) {
        space.moveTo(obj, x, y);
        grid.moveTo(obj, (int) x, (int) y);
    }

	public static NdPoint getRandomPoint() {
		return new NdPoint(
				RandomHelper.nextIntFromTo(0, (int) space.getDimensions().getWidth() - 1),
				RandomHelper.nextIntFromTo(0, (int) space.getDimensions().getHeight() - 1)
		);
	}
}
