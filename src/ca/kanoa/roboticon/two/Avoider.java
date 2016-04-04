package ca.kanoa.roboticon.two;

import ca.kanoa.roboticon.one.Direction;
import ca.kanoa.roboticon.utility.MotionController;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.navigation.DifferentialPilot;

public class Avoider extends MotionController {

	private static final double WHITE_MIN = 0.45d;
	private static final double BLACK_MAX = 0.20d;
	
	private EV3ColorSensor color;
	private float[] sample;
	private float lastSample;
	
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
			pilot.travel(-4);
			pilot.rotate(-90);
			pilot.travel(10);
			pilot.rotate(90);
			pilot.travel(16);
			pilot.rotate(90);
			pilot.travel(10);
			pilot.rotate(-90);
//			pilot.stop();
//			pilot.travel(-1);
//			pilot.rotate(-360, true);
//			while (onFriendly()) {
//				Delay.msDelay(5);
//			}
//			pilot.stop();
//			pilot.arc(6, -odometry.getPose().getHeading() + 90);
//			pilot.rotate(-90);
		} else {
			pilot.travel(8);
			pilot.rotate(-90);
			pilot.travel(-10);
			pilot.rotate(90);
			pilot.travel(-16);
			pilot.rotate(90);
			pilot.travel(-10);
			pilot.rotate(-90);
//			pilot.forward();
//			while (onFriendly()) {
//				Delay.msDelay(10);
//			}
//			pilot.quickStop();
//			pilot.travel(1);
//			pilot.quickStop();
//			pilot.rotate(360, true);
//			while (!onFriendly()) {
//				Delay.msDelay(10);
//			}
//			pilot.quickStop();
//			pilot.arc(9, odometry.getPose().getHeading() + 90);
//			pilot.rotate(90);
		}
		
		pilot.setTravelSpeed(ChallengeTwo.TRAVEL_SPEED);
		ChallengeTwo.getNavigator().timer.reset();
	}

	public boolean onFriendly() {
		float reading = readColor();
		if (reading < WHITE_MIN && reading > BLACK_MAX && lastSample < WHITE_MIN && lastSample > BLACK_MAX) {
			lastSample = reading;
			return true;
		} else {
			lastSample = reading;
			return false;
		}
	}
	
	/**
	 * Reads the color in Red mode.
	 */
	public float readColor() {
		color.fetchSample(sample, 0);
		return sample[0];
	}
	
}
