package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.Logger;

class Validators {
	static boolean doesLanguageExist(Context context, int langId) {
		return LanguageCollection.getLanguage(context, langId) != null;
	}

	static boolean validateSavedLanguage(Context context, int langId, String logTag) {
		if (!doesLanguageExist(context, langId)) {
			Logger.w(logTag, "Not saving invalid language with ID: " + langId);
			return false;
		}

		return true;
	}

	@SuppressWarnings("SameParameterValue")
	static boolean isIntInList(int number, ArrayList<Integer> list, String logTag, String logMsg) {
		if (!list.contains(number)) {
			Logger.w(logTag, logMsg);
			return false;
		}

		return true;
	}
}
