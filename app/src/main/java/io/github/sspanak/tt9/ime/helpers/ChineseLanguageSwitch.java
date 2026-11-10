package io.github.sspanak.tt9.ime.helpers;

/** A spelling preview must never become committed text when changing input languages. */
public final class ChineseLanguageSwitch {
	private ChineseLanguageSwitch() {}

	public static boolean shouldCommit(boolean enabled, boolean chinese, boolean changingLanguage,
		boolean typing, boolean spellingPreview, String candidate) {
		if (!enabled || !chinese || !changingLanguage || !typing || spellingPreview || candidate == null || candidate.isEmpty()) return false;
		return candidate.codePoints().allMatch(codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN);
	}
}
