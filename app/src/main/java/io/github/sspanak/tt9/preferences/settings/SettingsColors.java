package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.core.content.ContextCompat;

import io.github.sspanak.tt9.R;

public class SettingsColors extends SettingsHotkeys {
	SettingsColors(Context context) {
		super(context);
	}

	// Keyboard Panel
	public int getKeyboardBackground() {
		return ContextCompat.getColor(context, R.color.keyboard_background);
	}

	public int getKeyboardTextColor() {
		return ContextCompat.getColor(context, R.color.keyboard_text);
	}

	// Suggestions
	public int getSuggestionSelectedBackground() {
		return ContextCompat.getColor(context, R.color.suggestion_selected_background);
	}

	public int getSuggestionSelectedColor() {
		return ContextCompat.getColor(context, R.color.suggestion_selected_text);
	}

	public int getSuggestionSeparatorColor() {
		return ContextCompat.getColor(context, R.color.suggestion_separator);
	}
}
