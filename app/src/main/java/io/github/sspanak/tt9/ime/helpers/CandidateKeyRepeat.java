package io.github.sspanak.tt9.ime.helpers;

/** Rate limits native repeat events without timers or work continuing after key release. */
public final class CandidateKeyRepeat {
	private int key;
	private long lastDispatch;

	/** Shared speed labels have the same cadence in each independently configured candidate view. */
	public static int interval(String speed) {
		if ("slow".equals(speed)) return 120;
		return "fast".equals(speed) ? 30 : 60;
	}

	public boolean dispatch(int keyCode, long now, int interval) {
		if (keyCode != key) {
			key = keyCode;
			lastDispatch = now;
			return true;
		}
		if (now - lastDispatch < Math.max(1, interval)) return false;
		lastDispatch = now;
		return true;
	}

	public boolean release(int keyCode) {
		if (key == 0 || keyCode != key) return false;
		reset();
		return true;
	}

	public void reset() { key = 0; lastDispatch = 0; }
}
