package io.github.sspanak.tt9.colors;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsColors;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.ThemedContextBuilder;


abstract public class AbstractColorScheme {
	abstract public int getId();
	abstract public int getName();
	public boolean isDefault() { return false; }


	protected AbstractColorScheme(@NonNull Context context, int themeResId, @Nullable Boolean nightMode) {
		resolveColors(getStyledContext(context, themeResId, nightMode));
	}


	// keyboard panel
	protected int keyboardBackground = SettingsColors.DEFAULT_BACKGROUND_COLOR;
	protected int keyboardText = SettingsColors.DEFAULT_TEXT_COLOR;

	public int getKeyboardBackground() { return keyboardBackground; }
	public int getKeyboardText() { return keyboardText; }

	// suggestions
	protected int suggestionSelectedBackground = SettingsColors.DEFAULT_TEXT_COLOR; // invert to simulate highlight
	protected int suggestionSelectedColor = SettingsColors.DEFAULT_BACKGROUND_COLOR;
	protected int suggestionSeparatorColor = SettingsColors.DEFAULT_BACKGROUND_COLOR;

	public int getSuggestionSelectedBackground() { return suggestionSelectedBackground; }
	public int getSuggestionSelectedColor() { return suggestionSelectedColor; }
	public int getSuggestionSeparatorColor() { return suggestionSeparatorColor; }

	// standard keys
	protected int keyBackground = SettingsColors.DEFAULT_BACKGROUND_COLOR;
	protected int keyRipple = SettingsColors.DEFAULT_RIPPLE_COLOR;
	protected int keyText = SettingsColors.DEFAULT_TEXT_COLOR;
	protected int keyAlternativeText = keyText;

	public int getKeyBackground() { return keyBackground; }
	public int getKeyRipple() { return keyRipple; }
	public int getKeyText() { return keyText; }
	public int getKeyAlternativeText() { return keyAlternativeText; }

	// fn keys
	protected int keyFnBackground = SettingsColors.DEFAULT_BACKGROUND_COLOR;
	protected int keyFnRipple = SettingsColors.DEFAULT_RIPPLE_COLOR;
	protected int keyFnText = SettingsColors.DEFAULT_TEXT_COLOR;
	protected int keyFnAlternativeText = keyFnText;

	public int getKeyFnBackground() { return keyFnBackground; }
	public int getKeyFnRipple() { return keyFnRipple; }
	public int getKeyFnText() { return keyFnText; }
	public int getKeyFnAlternativeText() { return keyFnAlternativeText; }

	// LF4 key
	protected int keyLf4Background = SettingsColors.DEFAULT_BACKGROUND_COLOR;
	protected int keyLf4Ripple = SettingsColors.DEFAULT_RIPPLE_COLOR;
	protected int keyLf4Text = SettingsColors.DEFAULT_TEXT_COLOR;
	protected int keyLf4AlternativeText = keyLf4Text;

	public int getKeyLf4Background() { return keyLf4Background; }
	public int getKeyLf4Ripple() { return keyLf4Ripple; }
	public int getKeyLf4Text() { return keyLf4Text; }
	public int getKeyLf4AlternativeText() { return keyLf4AlternativeText; }

	// OK key
	protected int keyOkBackground = SettingsColors.DEFAULT_BACKGROUND_COLOR;
	protected int keyOkText = SettingsColors.DEFAULT_TEXT_COLOR;
	protected int keyOkRipple;

	public int getKeyOkBackground() { return keyOkBackground; }
	public int getKeyOkText() { return keyOkText; }
	public int getKeyOkRipple() { return keyOkRipple; }


	@NonNull
	private ContextThemeWrapper getStyledContext(@NonNull Context context, int themeResId, @Nullable Boolean nightMode) {
		ThemedContextBuilder builder = new ThemedContextBuilder()
			.setContext(context)
			.setConfiguration(context.getResources().getConfiguration())
			.setTheme(themeResId);

		if (nightMode != null) {
			builder.setNightMode(nightMode);
		} else {
			builder.setAutoNightMode();
		}

		return builder.build();
	}


	private void resolveColors(@NonNull ContextThemeWrapper styledCtx) {
		int[] definitions = new int[] {
			R.attr.colorKeyboardBackground,
			R.attr.colorKeyboardText,

			R.attr.colorSuggestionSelectedBackground,
			R.attr.colorSuggestionSelectedText,
			R.attr.colorSuggestionSeparator,

			R.attr.colorKeyNumBackground,
			R.attr.colorKeyNumRipple,
			R.attr.colorKeyNumText,
			R.attr.colorKeyNumAlternativeText,

			R.attr.colorKeyFnBackground,
			R.attr.colorKeyFnRipple,
			R.attr.colorKeyFnText,
			R.attr.colorKeyFnAlternativeText,

			R.attr.colorKeyLF4Background,
			R.attr.colorKeyLF4Ripple,
			R.attr.colorKeyLF4Text,
			R.attr.colorKeyLF4AlternativeText,

			R.attr.colorKeyOkBackground,
			R.attr.colorKeyOkRipple,
			R.attr.colorKeyOkText
		};

		try (TypedArray colors = styledCtx.obtainStyledAttributes(definitions)) {
			keyboardBackground = colors.getColor(0, keyBackground);
			keyboardText = colors.getColor(1, keyboardText);

			suggestionSelectedBackground = colors.getColor(2, suggestionSelectedBackground);
			suggestionSelectedColor = colors.getColor(3, suggestionSelectedColor);
			suggestionSeparatorColor = colors.getColor(4, suggestionSeparatorColor);

			keyBackground = colors.getColor(5, keyBackground);
			keyRipple = colors.getColor(6, keyRipple);
			keyText = colors.getColor(7, keyText);
			keyAlternativeText = colors.getColor(8, keyAlternativeText);

			keyFnBackground = colors.getColor(9, keyFnBackground);
			keyFnRipple = colors.getColor(10, keyFnRipple);
			keyFnText = colors.getColor(11, keyFnText);
			keyFnAlternativeText = colors.getColor(12, keyFnAlternativeText);

			keyLf4Background = colors.getColor(13, keyLf4Background);
			keyLf4Ripple = colors.getColor(14, keyLf4Ripple);
			keyLf4Text = colors.getColor(15, keyLf4Text);
			keyLf4AlternativeText = colors.getColor(16, keyLf4AlternativeText);

			keyOkBackground = colors.getColor(17, keyOkBackground);
			keyOkRipple = colors.getColor(18, keyOkRipple);
			keyOkText = colors.getColor(19, keyOkText);
		} catch (Exception e) {
			Logger.e(getClass().getSimpleName(), "Failed to resolve color scheme colors. " + e);
		}
	}
}
