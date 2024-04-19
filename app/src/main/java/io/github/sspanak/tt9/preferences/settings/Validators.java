package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.Logger;

class Validators {
	public static final int DEFAULT_INPUT_MODE = InputMode.MODE_PREDICTIVE;
	public static final int DEFAULT_TEXT_CASE = InputMode.CASE_LOWER;

	private static final ArrayList<Integer> validInputModes = new ArrayList<>(Arrays.asList(
		InputMode.MODE_123,
		InputMode.MODE_PREDICTIVE,
		InputMode.MODE_ABC
	));

	private static final ArrayList<Integer> validTextCases = new ArrayList<>(Arrays.asList(
		InputMode.CASE_LOWER,
		InputMode.CASE_UPPER,
		InputMode.CASE_CAPITALIZE
	));

	static boolean doesLanguageExist(Context context, int langId) {
		return LanguageCollection.getLanguage(context, langId) != null;
	}

	static boolean validateInputMode(int mode, String logTag, String logMsg) {
		return Validators.isIntInList(mode, validInputModes, logTag, logMsg);
	}

	static boolean validateInputLanguage(Context context, int langId, String logTag) {
		if (!doesLanguageExist(context, langId)) {
			Logger.w(logTag, "Not saving invalid language with ID: " + langId);
			return false;
		}

		return true;
	}

	static boolean validateTextCase(int textCase, String logTag, String logMsg) {
		return Validators.isIntInList(textCase, validTextCases, logTag, logMsg);
	}

	private static boolean isIntInList(int number, ArrayList<Integer> list, String logTag, String logMsg) {
		if (list == null || !list.contains(number)) {
			Logger.w(logTag, logMsg);
			return false;
		}

		return true;
	}
}
