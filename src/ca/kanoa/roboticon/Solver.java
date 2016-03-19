package ca.kanoa.roboticon;

import ca.kanoa.roboticon.Logger.Level;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class Solver {

	private static Solver solver;
	
	private Thread exitListener;
	private Logger logger;
	private ObstacleNavigator navigator;

	public Solver() {
		logger = new Logger(Level.DEBUG, false);
		logger.debug("Starting...");
		exitListener = new Thread(new ExitListener());
		navigator = new ObstacleNavigator(new EV3ColorSensor(LocalEV3.get().getPort("S4")), 
				new EV3MediumRegulatedMotor(MotorPort.D), new EV3UltrasonicSensor(LocalEV3.get().getPort("S2")));
		logger.debug("Welcome to Roboticon 2016!");
	}
	
	private void start() {
		exitListener.setDaemon(true);
		exitListener.start();
		
		navigator.start();
	}
	
	public static void main(String[] args) {
		solver = new Solver();
		solver.start();
	}
	
	public static void debug(String message) {
		solver.logger.debug(message);
	}
	
}
