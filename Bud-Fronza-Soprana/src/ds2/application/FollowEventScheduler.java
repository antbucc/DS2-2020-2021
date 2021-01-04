package ds2.application;

// Custom libraries
import ds2.simulator.QueuedScheduler;
import ds2.application.events.FollowEvent;

/**
 * This scheduler maintains a queue of FollowEvent. This is used to keep in memory the follow events needed to initialize the protocols
 */
public class FollowEventScheduler extends QueuedScheduler<FollowEvent> {
	/* Class needed to find this type of QueuedScheduler when needed */
}