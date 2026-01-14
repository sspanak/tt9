package io.github.sspanak.tt9.ime.modes;

public class InputModeKind {
	public static boolean isPassthrough(InputMode mode) {
		return mode != null && mode.getId() == InputMode.MODE_PASSTHROUGH;
	}

	public static boolean is123(InputMode mode) {
		return mode != null && mode.getId() == InputMode.MODE_123;
	}

	public static boolean isABC(InputMode mode) {
		return mode != null && mode.getId() == InputMode.MODE_ABC;
	}

	public static boolean isHiragana(InputMode mode) {
		return mode != null && mode.getId() == InputMode.MODE_HIRAGANA;
	}

	public static boolean isKatakana(InputMode mode) {
		return mode != null && mode.getId() == InputMode.MODE_KATAKANA;
	}

	public static boolean isNumeric(InputMode mode) {
		return isPassthrough(mode) || is123(mode);
	}

	public static boolean isPredictive(InputMode mode) {
		return mode != null && (
			mode.getId() == InputMode.MODE_PREDICTIVE ||
			mode.getId() == InputMode.MODE_HIRAGANA ||
			mode.getId() == InputMode.MODE_KATAKANA
		);
	}

	public static boolean isRecomposing(InputMode mode) {
		return mode instanceof ModeRecomposing;
	}
}
