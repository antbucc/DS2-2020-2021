package gossipingAppendOnlyLogs.motion;

import gossipingAppendOnlyLogs.actors.Person;

public abstract class MotionStrategy {

    protected final Person person;

    public MotionStrategy(Person person) {
        this.person = person;
    }

    public abstract void onTick();
}
