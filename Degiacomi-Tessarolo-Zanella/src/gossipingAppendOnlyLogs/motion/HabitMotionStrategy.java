package gossipingAppendOnlyLogs.motion;

import gossipingAppendOnlyLogs.RepastUtils;
import gossipingAppendOnlyLogs.actors.LAN;
import gossipingAppendOnlyLogs.actors.Person;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;

import java.util.ArrayList;
import java.util.List;

public class HabitMotionStrategy extends MotionStrategy {

	private final double STEP_SIZE = 0.5;
	private final double REACHED_TARGET_THRESHOLD = STEP_SIZE + 0.1;

	public HabitMotionStrategy(Person person) {
		super(person);
	}

	private NdPoint targetPoint;
	private final List<LAN> preferredLAN = new ArrayList<>();
	private NdPoint home;
	private int ticksSpentAtHome;
	private int ticksSpentAtLAN;
	private int remainingTicksAtCurrentPosition = -1;
	private LAN targetLAN;

	@Override
	public void onTick() {
		if (home == null) {
			init();
		}
		if (isStayingAtPlace()) {
			stayAtPlace();
		} else if (isTargetReached()) {
			onTargetReached();
		} else {
			moveTowardsTarget();
		}
	}

	private void init() {
		var params = RunEnvironment.getInstance().getParameters();

		this.home = RepastUtils.space.getLocation(person);

		pickPreferredLANs(params.getInteger("meanPrefLANs"), params.getInteger("stdPrefLANs"));

		var normalDistrHome = RandomHelper.createNormal(params.getInteger("meanTicksWaitingHome"), params.getInteger("stdTicksWaitingHome"));
		this.ticksSpentAtHome = Math.max(0, normalDistrHome.nextInt());

		var normalDistrLAN = RandomHelper.createNormal(params.getInteger("meanTicksWaiting"), params.getInteger("stdTicksWaiting"));
		this.ticksSpentAtLAN = Math.max(0, normalDistrLAN.nextInt());
	}

	private void pickPreferredLANs(int meanPrefLANs, int stdPrefLANs) {
		var preferredLANsCount = Math.max(1, RandomHelper.createNormal(meanPrefLANs, stdPrefLANs).nextInt());
		var LANs = RepastUtils.getAllLANsInGrid();
		var totLANs = LANs.size();
		for (var i = 0; i < preferredLANsCount; i++) {
			// Note that the same LAN could be sampled more times
			var LANIndex = RandomHelper.nextIntFromTo(0, totLANs - 1);
			preferredLAN.add(LANs.get(LANIndex));
		}
	}

	private boolean isStayingAtPlace() {
		return remainingTicksAtCurrentPosition != -1;
	}

	private void stayAtPlace() {
		remainingTicksAtCurrentPosition--;
		if (remainingTicksAtCurrentPosition == 0) {
			remainingTicksAtCurrentPosition = -1;
		}
	}

	private boolean isTargetReached() {
		return targetPoint == null;
	}

	private void onTargetReached() {
		if (targetLAN != null) {
			// I'm just arrived in a LAN
			targetPoint = home;
			remainingTicksAtCurrentPosition = ticksSpentAtLAN + RandomHelper.nextIntFromTo(0, 3);
			targetLAN = null;
		} else {
			// I'm just arrived at home
			targetLAN = preferredLAN.get(RandomHelper.nextIntFromTo(0, preferredLAN.size() - 1));
			targetPoint = RepastUtils.space.getLocation(targetLAN);
			remainingTicksAtCurrentPosition = ticksSpentAtHome + RandomHelper.nextIntFromTo(0, 3);
		}
	}

	private void moveTowardsTarget() {
		var personCoords = RepastUtils.space.getLocation(person).toDoubleArray(new double[2]);
		var targetCoords = targetPoint.toDoubleArray(new double[2]);

		for (int i = 0; i < 2; i++) {
			double difference = personCoords[i] - targetCoords[i];
			if (difference > 0) {
				personCoords[i] -= STEP_SIZE;
			} else {
				personCoords[i] += STEP_SIZE;
			}
		}

		RepastUtils.moveTo(person, personCoords[0], personCoords[1]);

		if (RepastUtils.space.getDistance(RepastUtils.space.getLocation(person), targetPoint) <= REACHED_TARGET_THRESHOLD) {
			targetPoint = null;
		}
	}
}
