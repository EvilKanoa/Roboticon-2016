package ca.kanoa.roboticon.one;

public class Scan {
	
	private long time;
	private int angle;
	private float color;
	
	public Scan(long time, int angle, float color) {
		this.angle = angle;
		this.color = color;
	}

	public double getTime() {
		return time;
	}
	
	public int getAngle() {
		return angle;
	}

	public float getColor() {
		return color;
	}

}
