package io.github.sspanak.tt9.languages;

import io.github.sspanak.tt9.util.TextTools;

abstract class SyllabaryLanguage extends Language {

	@Override
	public boolean isValidWord(String word) {
		if (!isSyllabary) {
			return false;
		}

		return (LanguageKind.isKorean(this) && TextTools.isHangul(word))
			|| (LanguageKind.isChinese(this) && TextTools.isChinese(word));
	}
}
