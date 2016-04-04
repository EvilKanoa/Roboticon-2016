package ca.kanoa.roboticon.two;

import ca.kanoa.roboticon.one.Direction;
import ca.kanoa.roboticon.utility.Convert;
import ca.kanoa.roboticon.utility.MotionController;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;
import lejos.utility.Stopwatch;

public class Navigator extends MotionController {

	private NavigatorStage stage;
	private EV3UltrasonicSensor sonar;
	private float[] sample;
	private Direction heading;
	private boolean firstSweep;
	public Stopwatch timer;
	
	public Navigator(DifferentialPilot pilot) {
		super(pilot);
		stage = NavigatorStage.START_TO_WALL;
		sample = new float[1];
		firstSweep = true;
		timer = new Stopwatch();
	}
	
	@Override
	public void start() {
		sonar = new EV3UltrasonicSensor(LocalEV3.get().getPort(ChallengeTwo.SONAR_PORT));
		heading = Direction.FORWARD;
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
	
	private void travelToWall() {
		if (!pilot.isMoving()) {
			pilot.forward();
		}
		if (readSonar() < 7 && ensureRead() < 15) {
			pilot.setTravelSpeed(30);
			pilot.forward();
			Delay.msDelay(2500);
			pilot.quickStop();
			pilot.setTravelSpeed(ChallengeTwo.TRAVEL_SPEED);
			pilot.steer(60, -25);
			pilot.travel(-3);
			pilot.rotate(-90);
			if (readSonar() > 34 && ensureRead() > 30) {
				heading = Direction.BACKWARD;
			} else {
				heading = Direction.FORWARD;
			}
			stage = NavigatorStage.WALL_TO_CORNER;
		}
	}

	private void travelToCorner() {
		if (!pilot.isMoving()) {
			moveNext();
		}
		if (heading == Direction.FORWARD ? readSonar() < 8 && ensureRead() < 18: readSonar() > 64 && ensureRead() > 50) {
			pilot.quickStop();
			pilot.rotate(90 * (heading == Direction.FORWARD ? 1 : -1));
			heading = heading == Direction.FORWARD ? Direction.BACKWARD : Direction.FORWARD;
			stage = heading == Direction.FORWARD ? NavigatorStage.SWEEPING_FORWARD : NavigatorStage.SWEEPING_BACKWARD;
		}
	}
	
	private void sweepBackward() {
		if (!pilot.isMoving()) {
			pilot.backward();
			firstSweep = false;
			timer.reset();
		}
		if ((readSonar() > 60 && ensureRead() > 45) || pilot.isStalled() || timer.elapsed() > 6000) {
			pilot.quickStop();
			pilot.setTravelSpeed(30);
			pilot.backward();
			Delay.msDelay(2000);
			pilot.setTravelSpeed(ChallengeTwo.TRAVEL_SPEED);
			pilot.quickStop();
			pilot.steer(90, ChallengeTwo.POINT_ANGLE + 5);
			pilot.steer(-100, -ChallengeTwo.POINT_ANGLE);
			pilot.steer(100, -ChallengeTwo.POINT_ANGLE);
			pilot.steer(-100, ChallengeTwo.POINT_ANGLE);
			heading = Direction.FORWARD;
			stage = NavigatorStage.SWEEPING_FORWARD;
		} else if (readSonar() > 60) {
			pilot.travel(-5);
		}
	}

	private void sweepForward() {
		if (!pilot.isMoving()) {
			pilot.forward();
			timer.reset();
		}
		if ((readSonar() < 6 && ensureRead() < 18) || pilot.isStalled() || timer.elapsed() > 5500) {
			pilot.quickStop();
			pilot.setTravelSpeed(30);
			pilot.forward();
			Delay.msDelay(2000);
			pilot.setTravelSpeed(ChallengeTwo.TRAVEL_SPEED);
			pilot.steer(100, -(ChallengeTwo.POINT_ANGLE + 28));
			pilot.steer(-100, ChallengeTwo.POINT_ANGLE);
			pilot.steer(100, ChallengeTwo.POINT_ANGLE);
			pilot.steer(-100, -ChallengeTwo.POINT_ANGLE);
			heading = Direction.BACKWARD;
			stage = NavigatorStage.SWEEPING_BACKWARD;
		} else if (readSonar() < 6) {
			pilot.travel(5);
		}
	}
	
	public boolean isFirstSweep() {
		return firstSweep;
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
		sonar.getDistanceMode().fetchSample(sample, 0);
		return Convert.centimeterToInch(sample[0] * 100);
	}
	
	public double ensureRead() {
		pilot.quickStop();
		pilot.rotate(20);
		double reading = readSonar();
		pilot.rotate(-20);
		return reading;
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
