package ca.kanoa.roboticon;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;

public class ObstacleNavigator implements Runnable {

	private static final int TRAVEL_SPEED = 30;
	private static final int ROTATE_SPEED = 50;
	private static final int ACCLERATION = 70;
	private static final int STEER_RATE = 60;
	private static final int MINIMUM_OBSTACLES_FOR_CORRECTION = 2;

	private Scanner scanner;
//	private WallNavigator wall;
//	private List<Scan> scans;
	private EV3LargeRegulatedMotor motorLeft, motorRight;
	private EV3UltrasonicSensor ultrasonic;
	private DifferentialPilot pilot;
	private OdometryPoseProvider odom;
	private int obstaclesAvoided;
	private boolean corrected;
	float[] sample;

	public ObstacleNavigator(EV3ColorSensor colorSensor, RegulatedMotor armMotor, EV3UltrasonicSensor ultrasonicSensor) {
		this.scanner = new Scanner(colorSensor, armMotor);
		this.ultrasonic = ultrasonicSensor;
//		wall = new WallNavigator(ultrasonicSensor);
//		scans = new LinkedList<>();

		motorLeft = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
		motorRight = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
		pilot = new DifferentialPilot(5.6, 13.47, motorLeft, motorRight, false);
		odom = new OdometryPoseProvider(pilot);
		obstaclesAvoided = 0;
		sample = new float[3];
	}

	@Override
	public void run() {
		while (true) {
			// first take a scan
			//scans.add(scanner.scan());
			if (scanner.isObstacle()) {
				pilot.quickStop();
				Solver.debug("y="+odom.getPose().getY());
				avoidObstacle();
			}
			if (!pilot.isMoving()) {
				pilot.forward();
			}
			if (!corrected && odom.getPose().getX() > 120) {
				//correctHeading();
				corrected = true;
			}
		}
	}

	private void correctHeading() {
		if (obstaclesAvoided >= MINIMUM_OBSTACLES_FOR_CORRECTION) {
			pilot.stop();
			pilot.setRotateSpeed(ROTATE_SPEED / 4);
			pilot.rotate(20);
			pilot.rotate(-40, true);
			
			float lastRead = readDistence();
			while (readDistence() < lastRead) {
				lastRead = readDistence();
			}
			pilot.quickStop();
			Solver.debug("rot d = " + odom.getPose().getHeading());
			odom.getPose().setHeading(0);
			
			pilot.forward();
		}
	}
	
	private float readDistence() {
		ultrasonic.getDistanceMode().fetchSample(sample, 0);
		return sample[0] * 100;
	}

	private void avoidObstacle() {
		pilot.setTravelSpeed(TRAVEL_SPEED / 2);
		Direction direction = scanner.getOpenDirection((int) odom.getPose().getY());
		while (direction == Direction.FORWARD && scanner.isObstacle()) {
			pilot.travel(-2);
			direction = scanner.getOpenDirection((int) odom.getPose().getY());
		}
		Solver.debug("d=" + direction.toString());
		
		float start = odom.getPose().getHeading();
		if (direction == Direction.RIGHT) {
			pilot.steer(-STEER_RATE);
		} else {
			pilot.steer(STEER_RATE);
		}
		while (scanner.isObstacle());
		
		float rotated = Math.abs(odom.getPose().getHeading() - start);
		pilot.arc(22 * (direction == Direction.LEFT ? -1 : 1), rotated * (direction == Direction.LEFT ? -1 : 1));
		
		if (Math.abs(start - odom.getPose().getHeading()) > 3) {
			pilot.rotate(start - odom.getPose().getHeading());
		}
		
		pilot.setTravelSpeed(TRAVEL_SPEED);
		float currX = odom.getPose().getX();
		pilot.forward();
		while (currX + 6 > odom.getPose().getX());
		obstaclesAvoided++;
	}

	public void start() {
		scanner.calibrate();
		pilot.setTravelSpeed(TRAVEL_SPEED);
		pilot.setRotateSpeed(ROTATE_SPEED);
		pilot.setAcceleration(ACCLERATION);
		pilot.reset();
		while (true) {
			this.run();
		}
	}

}
