package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import io.github.sspanak.tt9.preferences.screens.languages.SwitchAddWordsWithoutConfirmation;
import io.github.sspanak.tt9.preferences.screens.languages.SwitchRaiseImportLimits;

public class SettingsAddedWords extends BaseSettings {
	public final static int IMPORT_DEFAULT_MAX_FILE_LINES = 250;
	public final static int IMPORT_DEFAULT_MAX_WORDS = 1000;
	public final static int IMPORT_RAISED_MAX_FILE_LINES = 10_000;
	public final static int IMPORT_RAISED_MAX_WORDS = 100_000;

	SettingsAddedWords(Context context) {
		super(context);
	}

	public boolean getAddWordsNoConfirmation() {
		return prefs.getBoolean(SwitchAddWordsWithoutConfirmation.NAME, false);
	}

	public boolean getRaiseImportLimits() {
		return prefs.getBoolean(SwitchRaiseImportLimits.NAME, SwitchRaiseImportLimits.DEFAULT);
	}

	public int getImportWordsMaxFileLines() {
		return getRaiseImportLimits() ? IMPORT_RAISED_MAX_FILE_LINES : IMPORT_DEFAULT_MAX_FILE_LINES;
	}

	public int getImportWordsMaxWords() {
		return getRaiseImportLimits() ? IMPORT_RAISED_MAX_WORDS : IMPORT_DEFAULT_MAX_WORDS;
	}
}
