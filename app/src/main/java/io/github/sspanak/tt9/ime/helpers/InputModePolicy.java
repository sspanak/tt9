package io.github.sspanak.tt9.ime.helpers;

import java.util.ArrayList;
import java.util.Collection;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;

public class InputModePolicy {
	/** Chinese needs the dictionary to convert phonetic input, even when optional word prediction is off. */
	public static boolean isDictionaryEnabled(Language language, boolean predictiveModeEnabled) {
		return predictiveModeEnabled || LanguageKind.isChinese(language);
	}


	public static ArrayList<Integer> getAllowedModes(Collection<Integer> editorModes, Language language, boolean predictiveModeEnabled) {
		ArrayList<Integer> modes = new ArrayList<>(editorModes);

		// Kana are the Japanese equivalents of ABC. Only offer them if the editor accepts letters.
		if (LanguageKind.isJapanese(language) && modes.contains(InputMode.MODE_ABC)) {
			modes.add(InputMode.MODE_HIRAGANA);
			modes.add(InputMode.MODE_KATAKANA);
		}

		if (!language.hasABC()) {
			modes.remove((Integer) InputMode.MODE_ABC);
		}
		if (!isDictionaryEnabled(language, predictiveModeEnabled)) {
			modes.remove((Integer) InputMode.MODE_PREDICTIVE);
		}

		return modes;
	}


	/** Use editor capabilities, since the current language may have no usable text mode. */
	public static boolean canSwitchLanguage(boolean numericMode, Collection<Integer> editorModes) {
		return !numericMode || (
			editorModes.contains(InputMode.MODE_PREDICTIVE)
			|| editorModes.contains(InputMode.MODE_ABC)
			|| editorModes.contains(InputMode.MODE_HIRAGANA)
			|| editorModes.contains(InputMode.MODE_KATAKANA)
		);
	}
}
