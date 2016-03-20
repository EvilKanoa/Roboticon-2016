package ca.kanoa.roboticon.two;

import ca.kanoa.roboticon.one.Direction;
import ca.kanoa.roboticon.utility.Convert;
import ca.kanoa.roboticon.utility.MotionController;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;

public class Navigator extends MotionController {

	private NavigatorStage stage;
	private EV3UltrasonicSensor sonar;
	private float[] sample;
	private Direction heading;
	
	public Navigator(DifferentialPilot pilot) {
		super(pilot);
		stage = NavigatorStage.START_TO_WALL;
		sample = new float[3];
	}
	
	@Override
	public void start() {
		sonar = new EV3UltrasonicSensor(LocalEV3.get().getPort(ChallengeTwo.SONAR_PORT));
		sonar.setCurrentMode("Distence");
		while (true) {
			test();
			while (Button.ENTER.isUp());
		}
	}

	private void test() {
		pilot.steer(100, 90);
		pilot.steer(-100, -90);
		pilot.steer(100, -90);
		pilot.steer(-100, 90);
	}

	@Override
	public void update() {
		switch (stage) {
		case START_TO_WALL:
			travelToWall();
			break;
		case WALL_TO_CORNER:
			travelToCorner();
			break;
		case SWEEPING_FORWARD:
			sweepForward();
			break;
		case SWEEPING_BACKWARD:
			sweepBackward();
			break;
		}
	}
	
	private void sweepBackward() {
		if (!pilot.isMoving()) {
			pilot.backward();
		}
		if (readSonar() + ChallengeTwo.SONAR_DISPLACEMENT > 64) {
			pilot.steer(100, 90);
			pilot.steer(-100, -90);
			pilot.steer(100, -90);
			pilot.steer(-100, 90);
			heading = Direction.FORWARD;
			stage = NavigatorStage.SWEEPING_FORWARD;
		}
	}

	private void sweepForward() {
		if (!pilot.isMoving()) {
			pilot.forward();
		}
		if (readSonar() + ChallengeTwo.SONAR_DISPLACEMENT < 8) {
			pilot.steer(100, 90);
			pilot.steer(-100, -90);
			pilot.steer(100, -90);
			pilot.steer(-100, 90);
			heading = Direction.BACKWARD;
			stage = NavigatorStage.SWEEPING_BACKWARD;
		}
	}

	private void travelToCorner() {
		if (!pilot.isMoving()) {
			moveNext();
		}
		if (heading == Direction.FORWARD ? readSonar() > ChallengeTwo.TOUCH_DISTANCE : 
			readSonar() < 72 - (ChallengeTwo.TOUCH_DISTANCE + ChallengeTwo.SONAR_DISPLACEMENT)) {
			pilot.rotate(90 * (heading == Direction.FORWARD ? 1 : -1));
			heading = heading == Direction.FORWARD ? Direction.BACKWARD : Direction.FORWARD;
			stage = heading == Direction.FORWARD ? NavigatorStage.SWEEPING_FORWARD : NavigatorStage.SWEEPING_BACKWARD;
		}
	}

	private void travelToWall() {
		if (!pilot.isMoving()) {
			pilot.forward();
		}
		if (readSonar() <= ChallengeTwo.TOUCH_DISTANCE) {
			pilot.rotate(-90);
			if ((readSonar() + ChallengeTwo.SONAR_DISPLACEMENT) > 36) {
				heading = Direction.BACKWARD;
			} else {
				heading = Direction.FORWARD;
			}
			stage = NavigatorStage.WALL_TO_CORNER;
		}
	}
	
	private void moveNext() {
		switch (heading) {
		case FORWARD:
			pilot.forward();
			break;
		case BACKWARD:
			pilot.backward();
			break;
		default:
			break;
		}
	}
	
	/**
	 * Reads the current value of the sonar
	 * @return the distance of an object from the sonar in inches	
	 */
	public double readSonar() {
		sonar.fetchSample(sample, 0);
		return Convert.centimeterToInch(sample[0] * 100);
	}
	
	public Direction getHeading() {
		return heading;
	}
	
	public enum NavigatorStage {
		
		START_TO_WALL,
		WALL_TO_CORNER,
		SWEEPING_FORWARD,
		SWEEPING_BACKWARD;
		
	}
	
}
