package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.screens.modeAbc.DropDownAbcAutoAcceptTime;
import io.github.sspanak.tt9.preferences.screens.modePredictive.DropDownZeroKeyCharacter;

class SettingsTyping extends SettingsPunctuation {
	SettingsTyping(Context context) { super(context); }

	public int getAbcAutoAcceptTimeout() {
		int time = getStringifiedInt(DropDownAbcAutoAcceptTime.NAME, DropDownAbcAutoAcceptTime.DEFAULT);
		return time > 0 ? time + getKeyPadDebounceTime() : time;
	}
	public boolean getAutoSpace() { return prefs.getBoolean("auto_space", true); }
	public boolean getAutoTextCase() { return prefs.getBoolean("auto_text_case", true); }
	public boolean getAutoCapitalsAfterNewline() {
		return getAutoTextCase() && prefs.getBoolean("auto_capitals_after_newline", false);
	}

	public boolean getBackspaceAcceleration() {
		return prefs.getBoolean("backspace_acceleration", false);
	}

	public boolean getBackspaceRecomposing() {
		return prefs.getBoolean("backspace_recomposing", true);
	}

	@NonNull
	public String getDoubleZeroChar() {
		String character = prefs.getString(DropDownZeroKeyCharacter.NAME, DropDownZeroKeyCharacter.DEFAULT);

		// SharedPreferences return a corrupted string when using the real "\n"... :(
		return  character.equals("\\n") ? "\n" : character;
	}

	public boolean getPredictiveMode() {
		return prefs.getBoolean("pref_predictive_mode", true);
	}

	public boolean getPredictWordPairs() {
		return prefs.getBoolean("pref_predict_word_pairs", true);
	}

	public boolean getUpsideDownKeys() { return prefs.getBoolean("pref_upside_down_keys", false); }
}
