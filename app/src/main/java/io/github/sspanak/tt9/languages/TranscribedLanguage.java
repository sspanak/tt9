package io.github.sspanak.tt9.languages;

import io.github.sspanak.tt9.util.TextTools;

abstract class TranscribedLanguage extends Language implements Comparable<TranscribedLanguage> {

	@Override
	public boolean isValidWord(String word) {
		if (!isTranscribed) {
			return false;
		}

		return
			(LanguageKind.isKorean(this) && TextTools.isHangulText(word)) // because of the way Korean works, we only need to check if it's text.
			|| (LanguageKind.isChinese(this) && TextTools.isChinese(word))
			|| (LanguageKind.isJapanese(this) && TextTools.isJapanese(word));
	}


	/**
	 * These are usually languages with special scripts, such as Chinese, Korean, etc. They can't
	 * be sorted alphabetically so just put them at the end.
	 */
	protected String getSortingId() {
		return getName();
	}


	@Override
	public int compareTo(TranscribedLanguage other) {
		return getSortingId().compareTo(other.getSortingId());
	}
}
