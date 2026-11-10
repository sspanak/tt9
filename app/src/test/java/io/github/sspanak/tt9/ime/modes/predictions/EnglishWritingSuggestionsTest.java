package io.github.sspanak.tt9.ime.modes.predictions;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class EnglishWritingSuggestionsTest {
	@Test public void preservesExactAndLearnedWordsAheadOfShortcutsAndLongCompletions() {
		var merged = EnglishWritingSuggestions.merge(List.of("in", "go", "home", "immersion"), "46", "", true, "home"::equals);
		assertEquals(List.of("in", "go", "home", "I'm", "I am", "immersion"), merged.words());
		assertTrue(merged.isSynthetic("I'M"));
		assertFalse(merged.isSynthetic("home"));
	}

	@Test public void duplicateCanonicalWordsKeepTheirDictionarySpellingAndRank() {
		var merged = EnglishWritingSuggestions.merge(List.of("in", "go", "I'M", "i'm", "image"), "46", "", true, word -> false);
		assertEquals(List.of("in", "go", "I am", "I'M", "image"), merged.words());
		assertFalse(merged.isSynthetic("I'm"));
	}

	@Test public void requiresTheWholeAliasSequenceAndNeverChangesPunctuationKeySpellings() {
		for (String digits : List.of("4", "466", "416", "34", "3414", "8428", "842871", "")) {
			var merged = EnglishWritingSuggestions.merge(List.of("dictionary"), digits, "", true, word -> false);
			assertEquals(digits, digits.equals("34") ? List.of("dictionary", "e.g.") : List.of("dictionary"), merged.words());
		}
		assertEquals(List.of("I am"), EnglishWritingSuggestions.merge(List.of("I am"), "426", "i", true, word -> false).words());
	}

	@Test public void onlyTheDocumentedBoundedShortcutsProduceExpansions() {
		assertExpansions("46", "I'm", "I am");
		assertExpansions("426", "I'm", "I am");
		assertExpansions("84287", "that's", "that is");
		assertExpansions("3668", "don't", "do not");
		assertExpansions("2268", "can't", "cannot");
		assertExpansions("9668", "won't", "will not");
		assertExpansions("483", "I've", "I have");
		assertExpansions("34", "e.g.");
		assertExpansions("43", "i.e.");
		assertExpansions("2727", "ASAP", "as soon as possible");
	}

	@Test public void disabledOrExplicitlyFilteredPredictionsAreReturnedUnchanged() {
		List<String> words = List.of("in", "go", "home");
		assertEquals(words, EnglishWritingSuggestions.merge(words, "46", "", false, word -> true).words());
		assertEquals(words, EnglishWritingSuggestions.merge(words, "46", "i", true, word -> true).words());
		assertTrue(EnglishWritingSuggestions.merge(words, "46", "i", true, word -> true).syntheticWords().isEmpty());
	}

	@Test public void longCompletionsKeepAnOrdinaryDefaultBeforeAnyExpansion() {
		var merged = EnglishWritingSuggestions.merge(List.of("ivermectin", "iverson"), "483", "", true, word -> false);
		assertEquals(List.of("ivermectin", "I've", "I have", "iverson"), merged.words());
		assertFalse(merged.isSynthetic(merged.words().get(0)));
		assertEquals("ive", merged.words().get(0).substring(0, 3));
	}

	@Test public void callerMustProvideAnOrdinaryFallbackBeforeShortcutsCanAppear() {
		assertTrue(EnglishWritingSuggestions.merge(List.of(), "483", "", true, word -> false).words().isEmpty());
		assertEquals(List.of("ivd", "ive", "ivf", "I've", "I have"),
			EnglishWritingSuggestions.merge(List.of("ivd", "ive", "ivf"), "483", "", true, word -> false).words());
	}

	@Test public void eligibilityExcludesOtherLanguagesModesAndStructuredFields() {
		assertTrue(EnglishWritingSuggestions.isEnabled(true, true, true, true));
		assertFalse(EnglishWritingSuggestions.isEnabled(false, true, true, true));
		assertFalse(EnglishWritingSuggestions.isEnabled(true, false, true, true));
		assertFalse(EnglishWritingSuggestions.isEnabled(true, true, false, true));
		assertFalse(EnglishWritingSuggestions.isEnabled(true, true, true, false));
	}

	private void assertExpansions(String digits, String... expected) {
		String fallback = "x".repeat(digits.length());
		var result = EnglishWritingSuggestions.merge(List.of(fallback), digits, "", true, word -> false);
		assertEquals(fallback, result.words().get(0));
		assertEquals(List.of(expected), result.words().subList(1, result.words().size()));
	}
}
