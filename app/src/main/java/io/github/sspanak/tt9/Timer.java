package io.github.sspanak.tt9;

import java.util.HashMap;

public class Timer {
	private static final HashMap<String, Long> timers = new HashMap<>();

	public static void start() {
		start("default");
	}

	public static long stop() {
		return stop("default");
	}

	public static long restart() {
		long time = stop();
		start();
		return time;
	}

	public static long get(String timerName) {
		Long startTime = timers.get(timerName);
		if (startTime == null) {
			return -1;
		}
		return System.currentTimeMillis() - startTime;
	}

	public static void start(String timerName) {
		timers.put(timerName, System.currentTimeMillis());
	}

	public static long stop(String timerName) {
		long time = get(timerName);
		timers.remove(timerName);
		return time;
	}
}
