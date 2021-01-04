package gossipingAppendOnlyLogs.motion;

import gossipingAppendOnlyLogs.RepastUtils;
import gossipingAppendOnlyLogs.actors.Person;
import repast.simphony.space.continuous.NdPoint;

public class RandomMotionStrategy extends MotionStrategy {

    public RandomMotionStrategy(Person person) {
        super(person);
    }

    private NdPoint targetPoint;

    @Override
    public void onTick() {
        if (targetPoint == null) {
            targetPoint = RepastUtils.getRandomPoint();
        }
        moveTowardsTarget();
    }

    private void moveTowardsTarget() {
        var personCoords = RepastUtils.space.getLocation(person).toDoubleArray(new double[2]);
        var targetCoords = targetPoint.toDoubleArray(new double[2]);

        for (int i = 0; i < 2; i++) {
            double difference = personCoords[i] - targetCoords[i];
            if (difference > 0) {
            	personCoords[i] -= 0.1;
            } else {
            	personCoords[i] += 0.1;
            }
        }

        RepastUtils.moveTo(person, personCoords[0], personCoords[1]);

        if (RepastUtils.space.getDistance(RepastUtils.space.getLocation(person), targetPoint) < 0.2) {
            targetPoint = null;
        }
    }
}
