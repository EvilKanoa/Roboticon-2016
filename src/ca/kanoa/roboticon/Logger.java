package ca.kanoa.roboticon;

import lejos.utility.Stopwatch;

public class Logger {

	private Stopwatch watch;
	private Level displayLevel;
	private boolean logMilis;

	public Logger(Level displayLevel, boolean logMilis) {
		watch = new Stopwatch();
		watch.reset();
		this.displayLevel = displayLevel;
		this.logMilis = logMilis;
	}

	private void log(String message, Level level) {
		if (level.getLevel() >= displayLevel.getLevel()) {
			System.out.println(level.toString() + 
					formatTime(watch.elapsed()) + 
					message);
		}
	}

	private String formatTime(int time) {
		int minutes = (int) Math.floor(time / 60000);
		int seconds = (int) Math.floor((time - (minutes * 60000)) / 1000);
		int milis = (time - (minutes * 60000) - (seconds * 1000));
		return "[" + minutes + ":" + seconds + (logMilis ? ":" + milis : "") + "]";
	}

	public void debug(String msg) {
		log(msg, Level.DEBUG);
	}

	public void info(String msg) {
		log(msg, Level.INFO);
	}

	public void warning(String msg) {
		log(msg, Level.WARNING);
	}

	public void error(String msg) {
		log(msg, Level.ERROR);
	}

	public void severe(String msg) {
		log(msg, Level.SEVERE);
	}

	public Level getDisplayLevel() {
		return displayLevel;
	}

	public void setDisplayLevel(Level displayLevel) {
		this.displayLevel = displayLevel;
	}
	
	public enum Level {

		DEBUG(0),
		INFO(1),
		WARNING(2),
		ERROR(3),
		SEVERE(4);
		
		private int level;
		
		Level(int level) {
			this.level = level;
		}
		
		@Override
		public String toString() {
			return "[" + super.toString().toUpperCase().toCharArray()[0] + "]";
		}

		public int getLevel() {
			return level;
		}
		
	}


}
