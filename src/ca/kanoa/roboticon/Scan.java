package ca.kanoa.roboticon;

public class Scan {
	
	private long time;
	private int angle;
	private int color;
	
	public Scan(long time, int angle, int color) {
		this.angle = angle;
		this.color = color;
	}

	public double getTime() {
		return time;
	}
	
	public int getAngle() {
		return angle;
	}

	public int getColor() {
		return color;
	}

}
