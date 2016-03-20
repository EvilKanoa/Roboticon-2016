package ca.kanoa.roboticon.utility;

import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;

public abstract class MotionController {

	protected DifferentialPilot pilot;
	protected OdometryPoseProvider odometry;
	
	public MotionController(DifferentialPilot pilot) {
		this.pilot = pilot;
		odometry = new OdometryPoseProvider(pilot);
	}
	
	public DifferentialPilot getPilot() {
		return this.pilot;
	}
	
	public abstract void start();
	
	public abstract void update();
	
}
