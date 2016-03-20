package ca.kanoa.roboticon.two;

import ca.kanoa.roboticon.one.ExitListener;
import ca.kanoa.roboticon.one.Logger;
import ca.kanoa.roboticon.one.Logger.Level;
import ca.kanoa.roboticon.utility.Convert;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

public class ChallengeTwo {

	public static final String 		COLOR_PORT 			= "S3";
	public static final String 		SONAR_PORT 			= "S1";
	public static final double		TOUCH_DISTANCE		= 6.0;
	public static final double		SONAR_DISPLACEMENT 	= 4.0;
	
	private static final double 	TRAVEL_SPEED 	= 60;
	private static final double 	ROTATE_SPEED 	= 30;
	private static final int 		ACCELERATION 	= 50;
	private static final int 		TRACK_WIDTH 	= 7;
	private static final boolean 	MOTORS_REVERSED = true;
	
	private static ChallengeTwo program;
	
	private Thread exitListener;
	private Logger logger;
	private DifferentialPilot pilot;
	private Navigator navigator;
	private Avoider avoider;
	
	public ChallengeTwo() {
		exitListener = new Thread(new ExitListener());
		logger = new Logger(Level.DEBUG, false);
		pilot = new DifferentialPilot(Convert.centimeterToInch(5.60), TRACK_WIDTH, 
				new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")), 
				new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D")), MOTORS_REVERSED);
		navigator = new Navigator(pilot);
		avoider = new Avoider(pilot);
	}
	
	public static void main(String[] args) {
		program = new ChallengeTwo();
		program.start();
		while (true) {
			program.update();
		}
	}

	private void update() {
		avoider.update();
		navigator.update();
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
