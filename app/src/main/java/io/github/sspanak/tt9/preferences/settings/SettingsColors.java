package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import io.github.sspanak.tt9.colors.AbstractColorScheme;
import io.github.sspanak.tt9.colors.ColorSchemeSystem;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownColorScheme;

public class SettingsColors extends SettingsHotkeys {
	public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
	public static final int DEFAULT_RIPPLE_COLOR = Color.LTGRAY;
	public static final int DEFAULT_TEXT_COLOR = Color.BLACK;

	private final AbstractColorScheme defaultColorScheme;


	protected SettingsColors(Context context) {
		super(context);
		defaultColorScheme = new ColorSchemeSystem(context);
	}


	public String getColorSchemeId() {
		return prefs.getString(DropDownColorScheme.NAME, DropDownColorScheme.DEFAULT);
	}


	public void setColorScheme(@NonNull AbstractColorScheme scheme) {
		getPrefsEditor()
			.putString(DropDownColorScheme.NAME, String.valueOf(scheme.getId()))
			.putInt("pref_keyboard_background_color", scheme.getKeyboardBackground())
			.putInt("pref_keyboard_text_color", scheme.getKeyboardText())
			.putInt("pref_suggestion_selected_background", scheme.getSuggestionSelectedBackground())
			.putInt("pref_suggestion_selected_color", scheme.getSuggestionSelectedColor())
			.putInt("pref_suggestion_separator_color", scheme.getSuggestionSeparatorColor())
			.putInt("pref_key_background_color", scheme.getKeyBackground())
			.putInt("pref_key_corner_element_color", scheme.getKeyAlternativeText())
			.putInt("pref_key_ripple_color", scheme.getKeyRipple())
			.putInt("pref_key_text_color", scheme.getKeyText())
			.putInt("pref_key_fn_background_color", scheme.getKeyFnBackground())
			.putInt("pref_key_fn_corner_element_color", scheme.getKeyFnAlternativeText())
			.putInt("pref_key_fn_ripple_color", scheme.getKeyFnRipple())
			.putInt("pref_key_fn_text_color", scheme.getKeyFnText())
			.putInt("pref_key_lf4_background_color", scheme.getKeyLf4Background())
			.putInt("pref_key_lf4_corner_element_color", scheme.getKeyLf4AlternativeText())
			.putInt("pref_key_lf4_ripple_color", scheme.getKeyLf4Ripple())
			.putInt("pref_key_lf4_text_color", scheme.getKeyLf4Text())
			.putInt("pref_key_ok_background_color", scheme.getKeyOkBackground())
			.putInt("pref_key_ok_text_color", scheme.getKeyOkText())
			.putInt("pref_key_ok_ripple_color", scheme.getKeyOkRipple())
			.apply();
	}

	public boolean getDarkTheme() {
		return ColorUtils.calculateLuminance(getKeyboardBackground()) < 0.5;
	}

	// Keyboard Panel
	public int getKeyboardBackground() {
		return getColor("pref_keyboard_background_color", defaultColorScheme.getKeyboardBackground());
	}


	public int getKeyboardTextColor() {
		return getColor("pref_keyboard_text_color", defaultColorScheme.getKeyboardText());
	}


	// Suggestions
	public int getSuggestionSelectedBackground() {
		return getColor("pref_suggestion_selected_background", defaultColorScheme.getSuggestionSelectedBackground());
	}


	public int getSuggestionSelectedColor() {
		return getColor("pref_suggestion_selected_color", defaultColorScheme.getSuggestionSelectedColor());
	}


	public int getSuggestionSeparatorColor() {
		return getColor("pref_suggestion_separator_color", defaultColorScheme.getSuggestionSeparatorColor());
	}


	// Default key
	@NonNull
	public ColorStateList getKeyBackgroundColor() {
		return getStringifiedColorStateList("pref_key_background_color", defaultColorScheme.getKeyBackground());
	}


	@NonNull
	public ColorStateList getKeyRippleColor() {
		return getStringifiedColorStateList("pref_key_ripple_color", defaultColorScheme.getKeyRipple());
	}


	public int getKeyTextColor() {
		return getColor("pref_key_text_color", defaultColorScheme.getKeyText());
	}


	public int getKeyCornerElementColor() {
		return getColor("pref_key_corner_element_color", defaultColorScheme.getKeyAlternativeText());
	}


	// Fn Key
	@NonNull
	public ColorStateList getKeyFnBackgroundColor() {
		return getStringifiedColorStateList("pref_key_fn_background_color", defaultColorScheme.getKeyFnBackground());
	}


	@NonNull
	public ColorStateList getKeyFnRippleColor() {
		return getStringifiedColorStateList("pref_key_fn_ripple_color", defaultColorScheme.getKeyFnRipple());
	}


	public int getKeyFnTextColor() {
		return getColor("pref_key_fn_text_color", defaultColorScheme.getKeyFnText());
	}

	public int getKeyFnCornerElementColor() {
		return getColor("pref_key_fn_corner_element_color", defaultColorScheme.getKeyFnAlternativeText());
	}


	// LF4 Key
	@NonNull
	public ColorStateList getKeyLf4BackgroundColor() {
		return getStringifiedColorStateList("pref_key_lf4_background_color", defaultColorScheme.getKeyLf4Background());
	}


	@NonNull
	public ColorStateList getKeyLf4RippleColor() {
		return getStringifiedColorStateList("pref_key_lf4_ripple_color", defaultColorScheme.getKeyLf4Ripple());
	}


	public int getKeyLf4TextColor() {
		return getColor("pref_key_lf4_text_color", defaultColorScheme.getKeyLf4Text());
	}


	public int getKeyLf4CornerElementColor() {
		return getColor("pref_key_lf4_corner_element_color", defaultColorScheme.getKeyLf4AlternativeText());
	}


	// OK Key
	@NonNull
	public ColorStateList getKeyOkBackgroundColor() {
		return getStringifiedColorStateList("pref_key_ok_background_color", defaultColorScheme.getKeyOkBackground());
	}


	@NonNull
	public ColorStateList getKeyOkRippleColor() {
		return getStringifiedColorStateList("pref_key_ok_ripple_color", defaultColorScheme.getKeyOkRipple());
	}


	public int getKeyOkTextColor() {
		return getColor("pref_key_ok_text_color", defaultColorScheme.getKeyOkText());
	}


	// Helpers
	private int getColor(@NonNull String key, int defaultColor) {
		return prefs.getInt(key, defaultColor);
	}


	@NonNull
	private ColorStateList getStringifiedColorStateList(@NonNull String key, int defaultColor) {
		return ColorStateList.valueOf(getColor(key, defaultColor));
	}
}
