package io.github.sspanak.tt9.ui;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class StatusIcon {
	private static final HashMap<String, Integer> cache = new HashMap<>();
	private final int resourceId;


	private StatusIcon(@Nullable Context ctx, @Nullable SettingsStore settings, @Nullable InputMode mode, @Nullable Language language) {
		resourceId = resolveResourcePerMode(ctx, settings, mode, language);
	}


	private int resolveResourcePerMode(@Nullable Context ctx, @Nullable SettingsStore settings, @Nullable InputMode mode, @Nullable Language language) {
		if (language == null || mode == null || settings == null || InputModeKind.isPassthrough(mode) || !settings.isStatusIconEnabled()) {
			return 0;
		}

		if (InputModeKind.isHiragana(mode)) {
			return R.drawable.ic_lang_hiragana;
		} else if (InputModeKind.isKatakana(mode)) {
			return R.drawable.ic_lang_katakana;
		} else if (InputModeKind.is123(mode)) {
			return R.drawable.ic_lang_123;
		} else if (InputModeKind.isABC(mode)) {
			return resolveResource(ctx, language.getIconABC(), language.hasUpperCase() ? mode.getTextCase() : InputMode.CASE_UNDEFINED);
		} else if (InputModeKind.isPredictive(mode)) {
			return resolveResource(ctx, language.getIconT9(), language.hasUpperCase() ? mode.getTextCase() : InputMode.CASE_UNDEFINED);
		}

		return R.drawable.ic_keyboard;
	}


	private int resolveResource(Context ctx, String name, int textCase) {
		if (ctx == null || name == null) {
			return 0;
		}

		switch (textCase) {
			case InputMode.CASE_UPPER:
				name += "_up";
				break;
			case InputMode.CASE_LOWER:
				name += "_lo";
				break;
			case InputMode.CASE_CAPITALIZE:
				name += "_cp";
				break;
		}

		return ctx.getResources().getIdentifier("drawable/" + name, null, ctx.getPackageName());
	}


	@Nullable
	private static String getCacheKey(@Nullable InputMode mode, @Nullable Language language) {
		if (mode == null || language == null) {
			return null;
		}

		return mode.getId() + "_" + language.getId() + "_" + (language.hasUpperCase() ? mode.getTextCase() : InputMode.CASE_UNDEFINED);
	}


	public static int getResource(@Nullable Context ctx, @Nullable SettingsStore settings, @Nullable InputMode mode, @Nullable Language language) {
		final String cacheKey = getCacheKey(mode, language);
		Integer resId = cache.containsKey(cacheKey) ? cache.get(cacheKey) : Integer.valueOf(0);
		if (resId != null && resId != 0) {
			return resId;
		}

		resId = new StatusIcon(ctx, settings, mode, language).resourceId;
		if (resId != 0) {
			cache.put(cacheKey, resId);
		}

		return resId;
	}
}
