package io.github.sspanak.tt9.chinese;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/** Bounded syllable substitutions; never treats arbitrary substring replacements as Pinyin. */
public final class PinyinFuzzy {
	public static final int NL = 1;
	public static final int Z_ZH = 2;
	public static final int C_CH = 4;
	public static final int S_SH = 8;
	public static final int EN_ENG = 16;
	public static final int IN_ING = 32;
	private static final int MAX_QUERIES = 32;
	private static final int MAX_INPUT_LENGTH = 32;
	private static final String[][] PAIRS = {{"N", "L"}, {"Z", "ZH"}, {"C", "CH"}, {"S", "SH"}, {"EN", "ENG"}, {"IN", "ING"}};

	private PinyinFuzzy() {}

	private static final class SyllableKeys {
		static final Map<String, List<String>> BY_KEYS = new HashMap<>();
		static {
			for (String syllable : PinyinSyllables.all()) {
				BY_KEYS.computeIfAbsent(PinyinSyllables.getSequence(syllable), key -> new ArrayList<>()).add(syllable);
			}
		}
	}

	public static List<String> variants(String syllable, int options) {
		LinkedHashSet<String> result = new LinkedHashSet<>();
		result.add(syllable);
		ArrayList<String> pending = new ArrayList<>(result);
		// Revisit enabled rules after every valid change. For example, NEN -> NENG ->
		// LENG needs the final change before the initial change; LEN is not a syllable.
		// The known syllable set and the visited set bound this closure.
		for (int cursor = 0; cursor < pending.size(); cursor++) {
			String value = pending.get(cursor);
			for (int pair = 0; pair < PAIRS.length; pair++) {
				if ((options & (1 << pair)) == 0) continue;
				for (int side = 0; side < 2; side++) {
					String from = PAIRS[pair][side], to = PAIRS[pair][1 - side];
					String changed = pair < 4 && value.startsWith(from) ? to + value.substring(from.length())
						: pair >= 4 && value.endsWith(from) ? value.substring(0, value.length() - from.length()) + to : "";
					if (PinyinSyllables.isSyllable(changed) && result.add(changed)) pending.add(changed);
				}
			}
		}
		return new ArrayList<>(result);
	}

	/** Exact query remains first. Alternative sequence lookups use the existing SQLite position index. */
	public static List<String> sequences(String digits, String selectedReading, int options) {
		LinkedHashSet<String> result = new LinkedHashSet<>();
		result.add(digits);
		if (options == 0 || digits.isEmpty() || digits.length() > MAX_INPUT_LENGTH || !digits.matches("[2-9]+")) return new ArrayList<>(result);
		if (selectedReading != null && !selectedReading.isEmpty()) {
			try { result.add(PinyinSyllables.getSequence(selectedReading)); } catch (IllegalArgumentException ignored) {}
		}
		collectSequences(digits, 0, "", options, result, new int[]{0});
		return new ArrayList<>(result);
	}

	private static void collectSequences(String input, int offset, String output, int options, Set<String> result, int[] work) {
		if (result.size() >= MAX_QUERIES || ++work[0] > 4096) return;
		if (offset == input.length()) { result.add(output); return; }
		for (int end = Math.min(input.length(), offset + 6); end > offset; end--) {
			List<String> matches = SyllableKeys.BY_KEYS.get(input.substring(offset, end));
			if (matches == null) continue;
			for (String syllable : matches) {
				for (String variant : variants(syllable, options)) {
					collectSequences(input, end, output + PinyinSyllables.getSequence(variant), options, result, work);
					if (result.size() >= MAX_QUERIES || work[0] > 4096) return;
				}
			}
		}
	}

	/** Keeps a still-incomplete syllable in the same composition, using indexed word-prefix matches. */
	public static boolean matchesPrefix(String typedDigits, String selectedReading, String candidateReading) {
		try {
			String candidate = PinyinSyllables.compact(candidateReading);
			return !typedDigits.isEmpty() && PinyinSyllables.getSequence(candidate).startsWith(typedDigits)
				&& (selectedReading == null || selectedReading.isEmpty() || candidate.startsWith(PinyinSyllables.compact(selectedReading)));
		} catch (IllegalArgumentException ignored) { return false; }
	}

	/** Rejects unrelated spellings that happen to share an alternative T9 sequence. */
	public static boolean matches(String typedDigits, String selectedReading, String candidateReading, int options) {
		String compact;
		try { compact = PinyinSyllables.compact(candidateReading); } catch (IllegalArgumentException ignored) { return false; }
		if (selectedReading != null && !selectedReading.isEmpty()) {
			try { if (!compact.startsWith(PinyinSyllables.compact(selectedReading))) return false; }
			catch (IllegalArgumentException ignored) { return false; }
		}
		if (PinyinSyllables.getSequence(compact).equals(typedDigits)) return true;
		for (String segmentation : PinyinSyllables.segment(compact)) {
			if (matchesSegments(segmentation.split("'"), 0, typedDigits, 0, options)) return true;
		}
		return false;
	}

	private static boolean matchesSegments(String[] syllables, int syllable, String input, int offset, int options) {
		if (syllable == syllables.length) return offset == input.length();
		for (String variant : variants(syllables[syllable], options)) {
			String keys = PinyinSyllables.getSequence(variant);
			if (input.startsWith(keys, offset) && matchesSegments(syllables, syllable + 1, input, offset + keys.length(), options)) return true;
		}
		return false;
	}
}
