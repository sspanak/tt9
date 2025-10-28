package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import io.github.sspanak.tt9.colors.AbstractColorScheme;
import io.github.sspanak.tt9.colors.CollectionColorScheme;
import io.github.sspanak.tt9.preferences.screens.appearance.DropDownColorScheme;

public class SettingsColors extends SettingsHotkeys {
	public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
	public static final int DEFAULT_RIPPLE_COLOR = Color.LTGRAY;
	public static final int DEFAULT_TEXT_COLOR = Color.BLACK;

	@Nullable
	private static AbstractColorScheme colorScheme;


	protected SettingsColors(Context context) {
		super(context);
		colorScheme = CollectionColorScheme.get(context, getColorSchemeId());
	}


	public String getColorSchemeId() {
		return prefs.getString(DropDownColorScheme.NAME, DropDownColorScheme.DEFAULT);
	}


	public void setColorScheme(@NonNull AbstractColorScheme scheme) {
		colorScheme = scheme;
		getPrefsEditor()
			.putString(DropDownColorScheme.NAME, String.valueOf(scheme.getId()))
			.apply();
	}


	public boolean getDarkTheme() {
		return ColorUtils.calculateLuminance(getKeyboardBackground()) < 0.5;
	}


	// Keyboard Panel
	public int getKeyboardBackground() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyboardBackground();
	}


	public int getKeyboardTextColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyboardText();
	}


	// Suggestions
	public int getSuggestionSelectedBackground() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getSuggestionSelectedBackground();
	}


	public int getSuggestionSelectedColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getSuggestionSelectedColor();
	}


	public int getSuggestionSeparatorColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getSuggestionSeparatorColor();
	}


	// Default key
	@NonNull
	public ColorStateList getKeyBackgroundColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyBackground());
	}


	@NonNull
	public ColorStateList getKeyRippleColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyRipple());
	}


	public int getKeyTextColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyText();
	}


	public int getKeyCornerElementColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyAlternativeText();
	}


	// Fn Key
	@NonNull
	public ColorStateList getKeyFnBackgroundColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyFnBackground());
	}


	@NonNull
	public ColorStateList getKeyFnRippleColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyFnRipple());
	}


	public int getKeyFnTextColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyFnText();
	}

	public int getKeyFnCornerElementColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyFnAlternativeText();
	}


	// LF4 Key
	@NonNull
	public ColorStateList getKeyLf4BackgroundColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyLf4Background());
	}


	@NonNull
	public ColorStateList getKeyLf4RippleColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyLf4Ripple());
	}


	public int getKeyLf4TextColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyLf4Text();
	}


	public int getKeyLf4CornerElementColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyLf4AlternativeText();
	}


	// OK Key
	@NonNull
	public ColorStateList getKeyOkBackgroundColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyOkBackground());
	}


	@NonNull
	public ColorStateList getKeyOkRippleColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyOkRipple());
	}


	public int getKeyOkTextColor() {
		colorScheme = CollectionColorScheme.invalidate(context, colorScheme);
		return colorScheme.getKeyOkText();
	}


	// Helpers
//	private int getColor(@NonNull String key, int defaultColor) {
//		return prefs.getInt(key, defaultColor);
//	}
//
//
//	@NonNull
//	private ColorStateList getStringifiedColorStateList(@NonNull String key, int defaultColor) {
//		return ColorStateList.valueOf(getColor(key, defaultColor));
//	}
}
