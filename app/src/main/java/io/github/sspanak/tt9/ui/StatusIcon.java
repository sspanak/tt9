package io.github.sspanak.tt9.ui;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;

public class StatusIcon {
	private final int resourceId;

	public StatusIcon(@Nullable InputMode mode, @Nullable Language language) {
		if (mode == null || language == null) {
			resourceId = 0;
			return;
		}

		int resId = R.drawable.ic_keyboard;

		if (InputModeKind.isHiragana(mode)) {
			resId = R.drawable.ic_lang_hiragana;
		} else if (InputModeKind.isKatakana(mode)) {
			resId = R.drawable.ic_lang_katakana;
		} else if (InputModeKind.is123(mode)) {
			resId = R.drawable.ic_lang_123;
		} else if (InputModeKind.isABC(mode)) {
			if (LanguageKind.isCyrillic(language)) {
				resId = mode.getTextCase() == InputMode.CASE_UPPER ? R.drawable.ic_lang_cyrilic_up : R.drawable.ic_lang_cyrilic_lo;
			} else if (LanguageKind.isLatinBased(language)) {
				resId = mode.getTextCase() == InputMode.CASE_UPPER ? R.drawable.ic_lang_latin_up : R.drawable.ic_lang_latin_lo;
			}
		} else if (InputModeKind.isPredictive(mode)) {
			if (LanguageKind.isEnglish(language)) {
				resId = mode.getTextCase() == InputMode.CASE_UPPER ? R.drawable.ic_lang_en_up : R.drawable.ic_lang_en_lo;
				resId = mode.getTextCase() == InputMode.CASE_CAPITALIZE ? R.drawable.ic_lang_en_cp : resId;
			} else if (language.getId() == 231650) {
				resId = mode.getTextCase() == InputMode.CASE_UPPER ? R.drawable.ic_lang_bg_up : R.drawable.ic_lang_bg_lo;
				resId = mode.getTextCase() == InputMode.CASE_CAPITALIZE ? R.drawable.ic_lang_bg_cp : resId;
			} else if (LanguageKind.isJapanese(language)) {
				resId = R.drawable.ic_lang_kanji;
			} else if (LanguageKind.isChinesePinyin(language)) {
				resId = R.drawable.ic_lang_zh_pinyin;
			} else if (LanguageKind.isKorean(language)) {
				resId = R.drawable.ic_lang_kr;
			}
		}

		resourceId = resId;
	}


	public int getResourceId() {
		return resourceId;
	}
}
