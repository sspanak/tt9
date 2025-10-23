package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import io.github.sspanak.tt9.R;

public class SettingsColors extends SettingsHotkeys {
	public static final int DEFAULT_TEXT_COLOR = Color.BLACK;
	public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;

	SettingsColors(Context context) {
		super(context);
	}

	public boolean getDarkTheme() {
		return ColorUtils.calculateLuminance(getKeyboardBackground()) < 0.5;
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
