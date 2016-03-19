package ca.kanoa.roboticon;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.RegulatedMotor;
import lejos.utility.Delay;

public class Scanner {

	private static final int ARM_SPEED = 400;
	private static final int REVERSE_RANGE = 10;

//	private static final double WHITE_MIN = 0.60d;
//	private static final double BLACK_MAX = 0.20d;

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
		sensor.setCurrentMode("ColorID");

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
	
	public int read() {
		//sensor.getRedMode().fetchSample(sample, 0);
		return sensor.getColorID();
	}

	public boolean isObstacle() {
		//return read() < WHITE_MIN;
		return read() != Color.WHITE;
	}

	public Direction getOpenDirection() { 
		int leftAngle, rightAngle;
		
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
