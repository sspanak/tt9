package io.github.sspanak.tt9.ime.modes.predictions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.ime.helpers.SuggestionOps;
import io.github.sspanak.tt9.ime.helpers.TextField;

import static org.junit.Assert.*;

public class EnglishWritingPredictionsTest {
	@Test public void realPredictionCallbackMergesAfterDatabaseRankingAndBeforeLongCompletions() {
		TestPredictions predictions = new TestPredictions("46", Locale.ENGLISH, true);
		predictions.learned = Set.of("home");
		predictions.receive("go", "in", "home", "immersion");
		assertEquals(List.of("go", "in", "home", "I'm", "I am", "immersion"), predictions.getList());
		assertFalse(predictions.containsGeneratedWords());
	}

	@Test public void aliasesDoNotCreateDictionaryFrequencyOrPairUpdates() {
		TestPredictions predictions = new TestPredictions("46", Locale.ENGLISH, true);
		predictions.receive("in", "go", "immersion");
		predictions.onAccept("I'm", "416");
		predictions.onAccept("I am", "4026");
		assertTrue(predictions.priorityUpdates.isEmpty());
		assertTrue(predictions.pairUpdates.isEmpty());
		predictions.onAccept("go", "46");
		assertEquals(List.of("go:46"), predictions.priorityUpdates);
		assertEquals(List.of("go:46"), predictions.pairUpdates);
	}

	@Test public void normalPunctuationKeyContractionsKeepTheirLearningPath() {
		TestPredictions predictions = new TestPredictions("36618", Locale.ENGLISH, true);
		predictions.receive("dont", "don't");
		predictions.onAccept("don't", "36618");
		assertEquals(List.of("don't:36618"), predictions.priorityUpdates);
	}

	@Test public void actualCallbackHonorsLanguageSettingAndStemRestrictions() {
		for (TestPredictions predictions : List.of(
			new TestPredictions("46", Locale.ENGLISH, false), new TestPredictions("46", Locale.FRENCH, true))) {
			predictions.receive("in", "home");
			assertEquals(List.of("in", "home"), predictions.getList());
		}
		TestPredictions filtered = new TestPredictions("46", Locale.ENGLISH, true);
		filtered.setStem("i");
		filtered.receive("in", "immersion");
		assertEquals(List.of("in", "immersion"), filtered.getList());
	}

	@Test public void emptyDictionaryKeepsTheOriginalGeneratedFallbackBeforeAliases() {
		TestPredictions predictions = new TestPredictions("483", Locale.ENGLISH, true);
		predictions.receive();
		assertEquals(List.of("ivd", "ive", "ivf", "I've", "I have"), predictions.getList());
		assertTrue(predictions.containsGeneratedWords());
	}

	@Test public void defaultPreviewAndSpaceDoNotCommitATruncatedShortcut() {
		for (boolean noDictionaryWords : List.of(true, false)) {
			TestPredictions predictions = new TestPredictions("483", Locale.ENGLISH, true);
			if (noDictionaryWords) predictions.receive(); else predictions.receive("ivermectin", "iverson");
			RecordingTextField text = new RecordingTextField();
			TestSuggestionOps suggestions = new TestSuggestionOps(text, predictions.getList());
			text.preview = suggestions.getCurrent(new TestLanguage(Locale.ENGLISH), 3);
			assertEquals(noDictionaryWords ? "ivd" : "ive", text.preview);
			suggestions.acceptIncompleteAndKeepList(); // the existing space-key path
			assertEquals(noDictionaryWords ? "ivd" : "ive", text.committed);
			assertNotEquals("I'v", text.committed);
		}
	}

	@Test public void deliberateSelectionAndOkCommitTheWholeShortcut() {
		TestPredictions predictions = new TestPredictions("483", Locale.ENGLISH, true);
		predictions.receive();
		for (String alias : List.of("I've", "I have")) {
			RecordingTextField text = new RecordingTextField();
			TestSuggestionOps suggestions = new TestSuggestionOps(text, predictions.getList());
			text.preview = suggestions.getCurrent(new TestLanguage(Locale.ENGLISH), 3);
			suggestions.selection = predictions.getList().indexOf(alias);
			assertTrue(suggestions.selection > 0);
			assertEquals(alias, suggestions.acceptCurrent()); // OK completes the selected candidate
			assertEquals(alias, text.committed);
		}
	}

	private static class TestPredictions extends WordPredictions {
		Set<String> learned = Set.of();
		final List<String> pairUpdates = new ArrayList<>(), priorityUpdates = new ArrayList<>();
		TestPredictions(String digits, Locale locale, boolean allowed) {
			super(null);
			setLanguage(new TestLanguage(locale));
			setDigitSequence(digits);
			setInputWord("");
			setEnglishWritingAllowed(allowed);
		}
		void receive(String... words) { onDbWords(new ArrayList<>(Arrays.asList(words)), true); }
		@Override protected boolean isLearnedEnglishCompletion(String word) { return learned.contains(word); }
		@Override protected ArrayList<String> generateWordVariations(String word) {
			containsGeneratedWords = true;
			return new ArrayList<>(List.of("ivd", "ive", "ivf"));
		}
		@Override protected ArrayList<String> rearrangeByPairFrequency(ArrayList<String> words) { return words; }
		@Override protected void pairWithPreviousWord(String word, String sequence) { pairUpdates.add(word + ":" + sequence); }
		@Override protected void makeTopWord(String word, String sequence) { priorityUpdates.add(word + ":" + sequence); }
	}

	private static class TestSuggestionOps extends SuggestionOps {
		final List<String> words;
		int selection;
		TestSuggestionOps(TextField text, List<String> words) {
			super(null, null, null, null, null, text, null, null, null, null, null);
			this.words = words;
		}
		@Override public boolean isEmpty() { return words.isEmpty(); }
		@Override public String getCurrent() { return words.get(selection); }
	}

	private static class RecordingTextField extends TextField {
		String preview = "", committed = "";
		RecordingTextField() { super(null, null, null); }
		@Override public String getComposingText() { return preview; }
		@Override public void finishComposingText() { committed = preview; preview = ""; }
		@Override public void setText(String text) { committed = text; preview = ""; }
	}

	private static class TestLanguage extends NullLanguage {
		TestLanguage(Locale testLocale) { locale = testLocale; hasUpperCase = true; }
	}
}
