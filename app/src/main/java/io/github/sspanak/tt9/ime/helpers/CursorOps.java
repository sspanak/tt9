package io.github.sspanak.tt9.ime.helpers;

public class CursorOps {
	public static boolean isMovedManually(int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
		return
			candidatesStart != -1 && candidatesEnd != -1
			&& (newSelStart != candidatesEnd || newSelEnd != candidatesEnd);
	}

	public static boolean isInputReset(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
		return
			oldSelStart > 0 && oldSelEnd > 0
			&& newSelStart == 0 && newSelEnd == 0
			&& candidatesStart == -1 && candidatesEnd == -1;
	}
}
