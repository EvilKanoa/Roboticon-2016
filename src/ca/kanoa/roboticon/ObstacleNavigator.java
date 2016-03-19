package ca.kanoa.roboticon;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;

public class ObstacleNavigator implements Runnable {

	private static final int TRAVEL_SPEED = 25;
	private static final int ROTATE_SPEED = 45;
	private static final int MINIMUM_OBSTACLES_FOR_CORRECTION = 2;

	private Scanner scanner;
//	private WallNavigator wall;
//	private List<Scan> scans;
	private Thread thread;
	private EV3LargeRegulatedMotor motorLeft, motorRight;
	private DifferentialPilot pilot;
	private OdometryPoseProvider odom;
	private int obstaclesAvoided;
	private boolean corrected;

	public ObstacleNavigator(EV3ColorSensor colorSensor, RegulatedMotor armMotor, EV3UltrasonicSensor ultrasonicSensor) {
		this.scanner = new Scanner(colorSensor, armMotor);
//		wall = new WallNavigator(ultrasonicSensor);
//		scans = new LinkedList<>();
		thread = new Thread(this);

		motorLeft = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
		motorRight = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
		pilot = new DifferentialPilot(5.6, 13.47, motorLeft, motorRight, false);
		odom = new OdometryPoseProvider(pilot);
		obstaclesAvoided = 0;
	}

	@Override
	public void run() {
		while (true) {
			// first take a scan
			//scans.add(scanner.scan());
			if (scanner.isObstacle()) {
				pilot.quickStop();
				Solver.debug("y="+odom.getPose().getX());
				avoidObstacle();
			}
			if (!corrected && odom.getPose().getX() > 120) {
				correctHeading();
			}
		}
	}

	private void correctHeading() {
		if (obstaclesAvoided >= MINIMUM_OBSTACLES_FOR_CORRECTION) {
			pilot.stop();
			Sound.twoBeeps();
			pilot.forward();
		} else {
			Sound.beep();
		}
		corrected = true;
	}

	private void avoidObstacle() {
		pilot.setTravelSpeed(TRAVEL_SPEED / 2);
		Direction direction = scanner.getOpenDirection();
		while (direction == Direction.FORWARD) {
			pilot.travel(-2);
			direction = scanner.getOpenDirection();
		}
		Solver.debug("d=" + direction.toString());
		
		float start = odom.getPose().getHeading();
		if (direction == Direction.RIGHT) {
			pilot.rotate(-360, true);
		} else {
			pilot.rotate(360, true);
		}
		while (scanner.isObstacle());
		pilot.stop();
		pilot.steer(60 * (direction == Direction.LEFT ? 1 : -1), 50 * (direction == Direction.LEFT ? 1 : -1));
		pilot.rotate(start - odom.getPose().getHeading());
		pilot.setTravelSpeed(TRAVEL_SPEED);
		pilot.forward();
		obstaclesAvoided++;
	}

	public void start() {
		scanner.calibrate();
		thread.start();
		pilot.setTravelSpeed(TRAVEL_SPEED);
		pilot.setRotateSpeed(ROTATE_SPEED);
		pilot.forward();
	}

}
