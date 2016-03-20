package ca.kanoa.roboticon.one;

public class WallReading {

	private double distence;
	private long time;
	
	public WallReading(double distence, long time) {
		this.distence = distence;
		this.time = time;
	}
	
	public double getDistence() {
		return distence;
	}
	
	public long getTime() {
		return time;
	}
	
}
