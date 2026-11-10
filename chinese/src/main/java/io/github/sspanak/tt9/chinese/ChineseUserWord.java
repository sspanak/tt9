package io.github.sspanak.tt9.chinese;

import java.util.List;

/** Converts an explicitly transcribed user word to the existing PINYIN汉字 storage format. */
public record ChineseUserWord(String word, String reading, String sequence) {
	public static final int LANGUAGE_ID = 462106;

	public static ChineseUserWord create(String word, String reading) {
		String han = word == null ? "" : word.trim();
		if (han.isEmpty() || !han.codePoints().allMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN)) {
			throw new IllegalArgumentException("Enter Chinese characters only");
		}
		String syllables = PinyinSyllables.parseUserReading(reading, han.codePointCount(0, han.length()));
		return new ChineseUserWord(han, syllables, PinyinSyllables.getSequence(syllables));
	}

	public static ChineseUserWord fromStored(String stored) {
		int start = 0;
		while (start < stored.length() && stored.charAt(start) >= 'A' && stored.charAt(start) <= 'Z') start++;
		if (start == 0 || start == stored.length()) throw new IllegalArgumentException("A Chinese word requires its Pinyin reading");
		String han = stored.substring(start);
		String reading = stored.substring(0, start);
		if (!han.codePoints().allMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN)) {
			throw new IllegalArgumentException("Enter Chinese characters only");
		}
		List<String> variants = PinyinSyllables.segment(reading, han.codePointCount(0, han.length()));
		if (variants.isEmpty()) throw new IllegalArgumentException("The Pinyin reading does not match the Chinese word length");
		return new ChineseUserWord(han, variants.get(0), PinyinSyllables.getSequence(reading));
	}

	public String storedWord() { return PinyinSyllables.compact(reading) + word; }
	public String exportLine() { return word + "\t" + LANGUAGE_ID + "\t" + reading.replace('\'', ' '); }
}
