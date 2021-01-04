package gossipingAppendOnlyLogs;

import gossipingAppendOnlyLogs.actors.LAN;
import gossipingAppendOnlyLogs.actors.Person;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunListener;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProjectBuilder implements ContextBuilder<Object> {

    private static final int GRID_SIZE = 50;
    private static final int LANs_GRID_FACTOR = 10; // scaling factor of the LANs grid with the respect to the standard grid

    private final ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
    private final GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);

    private final StrategyFactory strategyFactory = new RepastConfigStrategyFactory();

    private Context<Object> context;

    private ContinuousSpace<Object> space;

    private Grid<Object> grid;

    private long simulationStartedAt;

    @Override
    public Context<Object> build(Context<Object> context) {
        this.context = context;
        context.setId("GossipingAppendOnlyLogs");
		System.out.println("Using random seed " + RandomHelper.getSeed());

        this.space = spaceFactory.createContinuousSpace(
                "space",
                context,
                new RandomCartesianAdder<Object>(),
                new repast.simphony.space.continuous.WrapAroundBorders(),
                GRID_SIZE,
                GRID_SIZE
        );
        this.grid = gridFactory.createGrid(
                "grid",
                context,
                new GridBuilderParameters<Object>(new WrapAroundBorders(), new SimpleGridAdder<Object>(),
                        true,
                        GRID_SIZE,
                        GRID_SIZE
                )
        );

        RepastUtils.init(grid, context, space);

        var params = RunEnvironment.getInstance().getParameters();

        createLANs(params);
        createPeople(params);

        int maxTicks = params.getInteger("stopAt");
		RunEnvironment.getInstance().endAt(maxTicks);
		RunEnvironment.getInstance().addRunListener(new RunListener() {
			@Override
			public void stopped() {
				System.out.println("Simulation stopped");
				System.out.println("Simulation took " + (System.currentTimeMillis() - simulationStartedAt) + "ms");
			}

			@Override
			public void paused() {
				System.out.println("Simulation paused");
			}

			@Override
			public void started() {
				System.out.println("Simulation started");
				simulationStartedAt = System.currentTimeMillis();
			}

			@Override
			public void restarted() {
				System.out.println("Simulation restarted");
			}
		});

        return context;
    }

    private void createLANs(Parameters params){
    	var grid_lans_size = GRID_SIZE / LANs_GRID_FACTOR;
    	var numPoints = (int)Math.pow(grid_lans_size, 2);
    	var possiblePositions = IntStream.rangeClosed(0, numPoints).boxed().collect(Collectors.toList());  

        int numLANs = params.getInteger("numLANs");
        for (int i = 0; i < numLANs; i++) {
            var id = "LAN" + i;
            LAN lan = new LAN(id);
            RepastUtils.addLAN(lan);
            
            var position = possiblePositions.remove(RandomHelper.nextIntFromTo(0, possiblePositions.size() - 1));
            var y = Math.ceil(position / grid_lans_size) * LANs_GRID_FACTOR;
            var x = (position % grid_lans_size) * LANs_GRID_FACTOR;
            RepastUtils.moveTo(lan, x, y);
        }
    }

    private void createPeople(Parameters params) {
        int numPeople = params.getInteger("numPeople");
        for (int i = 0; i < numPeople; i++) {
            var id = "Person" + i;
            Person p = new Person(id, CryptographyUtils.generateKeys(), strategyFactory);
            RepastUtils.addPerson(p);
            
            randomlyMoveAgent(p);
        }
    }

    private void randomlyMoveAgent(Object obj) {
        NdPoint pt = space.getLocation(obj);
        RepastUtils.moveTo(obj, pt.getX(), pt.getY());
    }
}
