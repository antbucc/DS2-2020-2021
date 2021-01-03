package scuttlebutt.context;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.InfiniteBorders;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.graph.Network;
import scuttlebutt.configuration.ScuttlebuttConfiguration;
import wavecast.configuration.WavecastConfiguration;
import wavecast.relay.Relay;

/*
 * Builder class for the Scuttlebutt context
 */
public class Builder implements ContextBuilder<Object> {

  /** ContinuousSpace that memorizes the location of the stores */
  private ContinuousSpace<Object> continuousSpace;

  /**
   * Network where each store is a node and each couple of wavecast neighbors is connected by an
   * edge
   */
  private Network<Object> network;

  /** Context that contains the stores (agents of the Scuttlebutt protocol) */
  private Context<Object> context;


  /*
   * Initialize the Scuttlebutt context, continuousSpace and network, creating one store per relay.
   * Store and relay are placed in the same coordinates in the space. Parameters are loaded.
   */
  @Override
  public Context<Object> build(Context<Object> context) {
    this.context = context;
    this.context.setId("scuttlebutt");
    this.context.addSubContext(new DefaultContext<>("wavecast"));
    wavecast.context.Builder wavecastBuilder = new wavecast.context.Builder();
    wavecastBuilder.build(this.context);

    ScuttlebuttConfiguration.load();

    // Initialize continuous space
    ContinuousSpaceFactory continuousSpaceFactory =
        ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
    this.continuousSpace = continuousSpaceFactory.createContinuousSpace("storeSpace", context,
        new SimpleCartesianAdder<Object>(), new InfiniteBorders<Object>(),
        WavecastConfiguration.SPACE_WIDTH, WavecastConfiguration.SPACE_HEIGHT);

    // Initialize the display network
    NetworkBuilder<Object> builder = new NetworkBuilder<Object>("storeNetwork", context, true);
    this.network = builder.buildNetwork();

    this.context.getSubContext("wavecast").getObjectsAsStream(Relay.class).map(obj -> (Relay) obj)
        .forEach(relay -> {
          wavecastBuilder.createStore(relay);
        });

    return context;
  }

}
