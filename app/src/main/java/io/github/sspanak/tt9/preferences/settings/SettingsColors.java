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
import io.github.sspanak.tt9.util.sys.SystemSettings;

public class SettingsColors extends SettingsHotkeys {
	public static final int DEFAULT_KEY_BACKGROUND_COLOR = Color.WHITE;
	public static final int DEFAULT_KEY_BORDER_COLOR = Color.TRANSPARENT;
	public static final int DEFAULT_KEY_RIPPLE_COLOR = Color.LTGRAY;
	public static final int DEFAULT_KEY_TEXT_COLOR = Color.BLACK;

	@Nullable
	protected static AbstractColorScheme colorScheme;


	protected SettingsColors(Context context) {
		super(context);
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


	public void setPreviewScheme(@NonNull AbstractColorScheme scheme) {
		colorScheme = scheme;
	}


	public boolean getDarkTheme() {
		return ColorUtils.calculateLuminance(getKeyboardBackground()) < 0.5;
	}


	// Keyboard Panel
	public int getKeyboardBackground() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyboardBackground();
	}


	public int getKeyboardTextColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyboardText();
	}


	// Suggestions
	public int getSuggestionSelectedBackground() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getSuggestionSelectedBackground();
	}


	public int getSuggestionSelectedColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getSuggestionSelectedColor();
	}


	public int getSuggestionSeparatorColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getSuggestionSeparatorColor();
	}


	// Default key
	@NonNull
	public ColorStateList getKeyBackgroundColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyBackground());
	}


	@NonNull
	public ColorStateList getKeyBorderColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyBorder());
	}


	@NonNull
	public ColorStateList getKeyRippleColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyRipple());
	}


	public int getKeyTextColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyText();
	}


	public int getKeyCornerElementColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyAlternativeText();
	}


	// Fn Key
	@NonNull
	public ColorStateList getKeyFnBackgroundColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyFnBackground());
	}


	@NonNull
	public ColorStateList getKeyFnBorderColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyFnBorder());
	}


	@NonNull
	public ColorStateList getKeyFnRippleColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyFnRipple());
	}


	public int getKeyFnTextColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyFnText();
	}

	public int getKeyFnCornerElementColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyFnAlternativeText();
	}


	// LF4 Key
	@NonNull
	public ColorStateList getKeyLf4BackgroundColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyLf4Background());
	}


	@NonNull
	public ColorStateList getKeyLf4BorderColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyLf4Border());
	}


	@NonNull
	public ColorStateList getKeyLf4RippleColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyLf4Ripple());
	}


	public int getKeyLf4TextColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyLf4Text();
	}


	public int getKeyLf4CornerElementColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyLf4AlternativeText();
	}


	// OK Key
	@NonNull
	public ColorStateList getKeyOkBackgroundColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyOkBackground());
	}


	@NonNull
	public ColorStateList getKeyOkBorderColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyOkBorder());
	}


	@NonNull
	public ColorStateList getKeyOkRippleColor() {
		colorScheme = invalidateScheme(colorScheme);
		return ColorStateList.valueOf(colorScheme.getKeyOkRipple());
	}


	public int getKeyOkTextColor() {
		colorScheme = invalidateScheme(colorScheme);
		return colorScheme.getKeyOkText();
	}


	// Helpers

	@NonNull
	protected AbstractColorScheme invalidateScheme(@Nullable AbstractColorScheme scheme) {
		if (scheme != null && scheme.getNightModeTag() == SystemSettings.isNightModeOn(context)) {
			return scheme;
		}

		return CollectionColorScheme.get(context, getColorSchemeId());
	}


	public void reloadColorScheme() {
		colorScheme = null;
	}
}
