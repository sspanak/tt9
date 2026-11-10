package io.github.sspanak.tt9.ime.modes.predictions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/** Bounded English shortcuts, kept separate from dictionary words and their learned frequencies. */
public final class EnglishWritingSuggestions {
	private static final Map<String, List<String>> SHORTCUTS = Map.ofEntries(
		Map.entry("46", List.of("I'm", "I am")), // im
		Map.entry("426", List.of("I'm", "I am")), // iam
		Map.entry("84287", List.of("that's", "that is")), // thats
		Map.entry("3668", List.of("don't", "do not")), // dont
		Map.entry("2268", List.of("can't", "cannot")), // cant
		Map.entry("9668", List.of("won't", "will not")), // wont
		Map.entry("483", List.of("I've", "I have")), // ive
		Map.entry("34", List.of("e.g.")), // eg
		Map.entry("43", List.of("i.e.")), // ie
		Map.entry("2727", List.of("ASAP", "as soon as possible")) // asap
	);
	private EnglishWritingSuggestions() {}

	public static boolean isEnabled(boolean enabled, boolean english, boolean predictive, boolean ordinaryField) {
		return enabled && english && predictive && ordinaryField;
	}

	public record Result(ArrayList<String> words, Set<String> syntheticWords) {
		public boolean isSynthetic(String word) { return syntheticWords.contains(word.toLowerCase(Locale.ROOT)); }
	}

	/** Keep an ordinary default; shortcuts require selection before they can replace the typed preview. */
	public static Result merge(List<String> dictionaryWords, String digits, String stem, boolean enabled, Predicate<String> learnedWord) {
		List<String> expansions = enabled && stem.isEmpty() ? SHORTCUTS.get(digits) : null;
		if (expansions == null || dictionaryWords.isEmpty()) return new Result(new ArrayList<>(dictionaryWords), Set.of());
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		Set<String> dictionary = new HashSet<>(), synthetic = new HashSet<>();
		for (String word : dictionaryWords) {
			dictionary.add(word.toLowerCase(Locale.ROOT));
			if (word.length() <= digits.length() || learnedWord.test(word)) add(result, word);
		}
		// With only long completions, preserve the original first preview and space behavior.
		if (result.isEmpty()) add(result, dictionaryWords.get(0));
		for (String word : expansions) {
			String key = word.toLowerCase(Locale.ROOT);
			// Existing dictionary entries keep their own position and spelling.
			if (!dictionary.contains(key)) {
				add(result, word);
				synthetic.add(key);
			}
		}
		for (String word : dictionaryWords) add(result, word);
		return new Result(new ArrayList<>(result.values()), Set.copyOf(synthetic));
	}

	private static void add(Map<String, String> words, String word) {
		words.putIfAbsent(word.toLowerCase(Locale.ROOT), word);
	}

}
