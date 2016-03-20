package ca.kanoa.roboticon.utility;

public class Convert {

	public static double feetToMeter(double feet) {
		return feet * 0.3048;
	}
	
	public static double meterToFeet(double meter) {
		return meter * 3.28084;
	}
	
	public static double centimeterToInch(double centimeter) {
		return centimeter * 0.393701;
	}
	
	public static double inchToCentimeter(double inch) {
		return inch * 2.54;
	}
	
}
