package ca.kanoa.roboticon;

import lejos.hardware.Button;

public class ExitListener implements Runnable {
	
	@Override
	public void run() {
		while(true) {
			if (Button.ESCAPE.isDown()) {
				System.exit(0);
			}
		}
	}
	
}
