package io.github.sspanak.tt9.chinese;

import io.github.sspanak.tt9.ime.modes.helpers.PinyinSequenceEditor;

/** Initializes editing from either a complete reading or the letters actually typed so far. */
final class PinyinEditReading {
	private PinyinEditReading() {}

	static PinyinSequenceEditor create(String digits, String selectedReading, int fuzzyOptions) {
		String compact = PinyinSyllables.compact(selectedReading);
		String selectedDigits = PinyinSyllables.getSequence(compact);
		if (PinyinFuzzy.matches(digits, "", compact, fuzzyOptions)) {
			return new PinyinSequenceEditor(selectedDigits, compact);
		}
		// A prefix query can show RI'LE for 745. Edit RIL, retaining only the three
		// typed letters; a completion is not permission to insert its trailing E.
		if (!digits.isEmpty() && selectedDigits.startsWith(digits) && digits.length() < compact.length()) {
			return new PinyinSequenceEditor(digits, compact.substring(0, digits.length()));
		}
		throw new IllegalArgumentException("The selected reading does not match the typed key sequence");
	}
}
