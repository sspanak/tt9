package io.github.sspanak.tt9.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;

public class StatusIcon {
	private final int resourceId;

	public StatusIcon(@Nullable Context ctx, @Nullable InputMode mode, @Nullable Language language) {
		resourceId = (mode == null || language == null) ? 0 : resolveResourcePerMode(ctx, mode, language);
	}

	private int resolveResourcePerMode(@Nullable Context ctx, @NonNull InputMode mode, @NonNull Language language) {
		int resId = 0;

		if (InputModeKind.isHiragana(mode)) {
			resId = R.drawable.ic_lang_hiragana;
		} else if (InputModeKind.isKatakana(mode)) {
			resId = R.drawable.ic_lang_katakana;
		} else if (InputModeKind.is123(mode)) {
			resId = R.drawable.ic_lang_123;
		} else if (InputModeKind.isABC(mode)) {
			resId = resolveResource(ctx, language.getIconABC(), language.hasUpperCase() ? mode.getTextCase() : InputMode.CASE_UNDEFINED);
		} else if (InputModeKind.isPredictive(mode)) {
			resId = resolveResource(ctx, language.getIconT9(), language.hasUpperCase() ? mode.getTextCase() : InputMode.CASE_UNDEFINED);
		}

		return resId == 0 ? R.drawable.ic_keyboard : resId;
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

	public int getResourceId() {
		return resourceId;
	}
}
