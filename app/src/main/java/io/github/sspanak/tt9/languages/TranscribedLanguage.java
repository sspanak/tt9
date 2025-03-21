package io.github.sspanak.tt9.languages;

import io.github.sspanak.tt9.util.TextTools;

abstract class TranscribedLanguage extends Language implements Comparable<TranscribedLanguage> {

	@Override
	public boolean isValidWord(String word) {
		if (!isTranscribed) {
			return false;
		}

		return (LanguageKind.isKorean(this) && TextTools.isHangul(word))
			|| (LanguageKind.isChinese(this) && TextTools.isChinese(word));
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
