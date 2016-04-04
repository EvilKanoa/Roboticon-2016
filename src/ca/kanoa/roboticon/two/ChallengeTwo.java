package ca.kanoa.roboticon.two;

import ca.kanoa.roboticon.one.ExitListener;
import ca.kanoa.roboticon.one.Logger;
import ca.kanoa.roboticon.one.Logger.Level;
import ca.kanoa.roboticon.utility.Convert;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class ChallengeTwo {

	public static final String 		COLOR_PORT 			= "S3";
	public static final String 		SONAR_PORT 			= "S2";
	public static final int			POINT_ANGLE			= 55;
	
	public static final double 		TRAVEL_SPEED 		= 8.3;
	public static final double 		ROTATE_SPEED 		= 50;
	private static final int 		ACCELERATION 		= 20;
	private static final double		TRACK_WIDTH 		= 16.00;
	private static final boolean 	MOTORS_REVERSED 	= false;
	private static final double		DRIFT_CORRECTION	= -0.0;
	
	private static ChallengeTwo program;
	
	private Thread exitListener;
	private Logger logger;
	private DifferentialPilot pilot;
	private Navigator navigator;
	private Avoider avoider;
	
	public ChallengeTwo() {
		exitListener = new Thread(new ExitListener());
		logger = new Logger(Level.DEBUG, false);
		pilot = new DifferentialPilot(Convert.centimeterToInch(5.6), Convert.centimeterToInch(5.6 + DRIFT_CORRECTION), 
				Convert.centimeterToInch(TRACK_WIDTH), 
				new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")), 
				new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D")), MOTORS_REVERSED);
		navigator = new Navigator(pilot);
		avoider = new Avoider(pilot);
	}
	
	public static void main(String[] args) {
		program = new ChallengeTwo();
		program.start();
		Sound.beep();
		program.logger.warning("press start");
		while (Button.ENTER.isUp());
		Delay.msDelay(800);
		while (true) {
			program.update();
		}
	}

	private void update() {
//		if (!pilot.isMoving()) {
//			while (Button.ENTER.isUp());
//			pilot.backward();
//		}
		avoider.update();
		navigator.update();
		Delay.msDelay(30);
	}

	private void start() {
		debug("start...");
		
		exitListener.setDaemon(true);
		exitListener.start();
		
		pilot.setTravelSpeed(TRAVEL_SPEED);
		pilot.setRotateSpeed(ROTATE_SPEED);
		pilot.setAcceleration(ACCELERATION);
		
		avoider.start();
		navigator.start();
		
		debug("started");
	}
	
	public static Navigator getNavigator() {
		return program.navigator;
	}
	
	public static void debug(String message) {
		if (program != null && program.logger != null) {
			program.logger.debug(message);
		}
	}
	
}
