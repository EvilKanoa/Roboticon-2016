package ca.kanoa.roboticon.one;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.RegulatedMotor;

public class Scanner {

	private static final int ARM_SPEED = 400;
	private static final int REVERSE_RANGE = 10;

	private static final double WHITE_MIN = 0.35d;

	private EV3ColorSensor sensor;
	private RegulatedMotor motor;
	private int rangeOfMotion;
	float[] sample;

	public Scanner(EV3ColorSensor colorSensor, RegulatedMotor armMotor) {
		this.sensor = colorSensor;
		this.motor = armMotor;
		sample = new float[1];
	}

	public void calibrate() {
		sensor.setCurrentMode("Red");

		// calibrate positions
		motor.setSpeed(ARM_SPEED);
		motor.backward();
		while (!motor.isStalled());
		motor.flt();
		motor.resetTachoCount();
		motor.forward();
		while(!motor.isStalled());
		motor.flt();

		rangeOfMotion = motor.getTachoCount();
		motor.rotate(rangeOfMotion / -2);

		Solver.debug("Arm ROM = " + rangeOfMotion);
	}

	/**
	 * Returns the current angle of the sensor arm
	 * @return The angle from the center
	 */
	public int getAngle() {
		return motor.getTachoCount() - (rangeOfMotion / 2);
	}

	public Scan scan() {
		//sensor.getRedMode().fetchSample(sample, 0);
		return new Scan(System.currentTimeMillis(), getAngle(), read());
	}
	
	public float read() {
		sensor.getRedMode().fetchSample(sample, 0);
		return sample[0];
	}

	public boolean isObstacle() {
		return read() < WHITE_MIN;
	}

	public Direction getOpenDirection() { 
		int leftAngle = 0, rightAngle = 0;
		
		motor.forward();
		while(motor.getTachoCount() < (rangeOfMotion - REVERSE_RANGE) && isObstacle());
		leftAngle = isObstacle() ? 360 : motor.getTachoCount() - (rangeOfMotion / 2);
		motor.rotateTo(rangeOfMotion / 2);
		
		motor.backward();
		while(motor.getTachoCount() > REVERSE_RANGE  && isObstacle());
		rightAngle = isObstacle() ? 360 : (rangeOfMotion / 2) - motor.getTachoCount();
		
		motor.rotateTo(rangeOfMotion / 2);
		motor.stop();
		
		if (leftAngle == rightAngle) {
			return Direction.FORWARD;
		} else if (leftAngle < rightAngle) {
			return Direction.LEFT;
		} else {
			return Direction.RIGHT;
		}
	}

}
