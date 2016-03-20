package ca.kanoa.roboticon.two;

import ca.kanoa.roboticon.one.Direction;
import ca.kanoa.roboticon.utility.MotionController;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.navigation.DifferentialPilot;

public class Avoider extends MotionController {

	private static final double WHITE_MIN = 0.70d;
	private static final double BLACK_MAX = 0.20d;
	
	private EV3ColorSensor color;
	private float[] sample;
	
	public Avoider(DifferentialPilot pilot) {
		super(pilot);
		sample = new float[3];
	}

	@Override
	public void start() {
		color = new EV3ColorSensor(LocalEV3.get().getPort(ChallengeTwo.COLOR_PORT));
		color.setCurrentMode("Red");
	}

	@Override
	public void update() {
		if (onFriendly()) {
			passObstacle();
		}
	}
	
	private void passObstacle() {
		pilot.quickStop();
		odometry.getPose().setHeading(0);
		if (ChallengeTwo.getNavigator().getHeading() == Direction.FORWARD) {
			pilot.travel(1);
			pilot.rotateRight();
			while (onFriendly());
			pilot.arc(13, -odometry.getPose().getHeading() + 90);
			pilot.rotate(-90);
		} else {
			pilot.travel(-2);
			pilot.rotateLeft();
			while (!onFriendly());
			pilot.arc(13, odometry.getPose().getHeading() + 90);
			pilot.rotate(90);
		}
	}

	public boolean onFriendly() {
		float reading = readColor();
		return reading < WHITE_MIN && reading > BLACK_MAX;
	}
	
	/**
	 * Reads the color in Red mode.
	 */
	public float readColor() {
		color.fetchSample(sample, 0);
		return sample[0];
	}
	
}
