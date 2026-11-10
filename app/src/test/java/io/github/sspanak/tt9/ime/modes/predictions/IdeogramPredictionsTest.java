package io.github.sspanak.tt9.ime.modes.predictions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;

import static org.junit.Assert.assertEquals;

public class IdeogramPredictionsTest {
	@Test
	public void duplicateNativeCandidatesKeepTheirFirstRankedPosition() {
		TestPredictions predictions = new TestPredictions();
		predictions.receive("LV绿", "LU路", "LU绿", "LV吕", "LU路");

		predictions.stripTranscriptions();

		assertEquals(Arrays.asList("绿", "路", "吕"), predictions.getList());
		assertEquals(Arrays.asList("LV绿", "LU路", "LU绿", "LV吕", "LU路"), predictions.transcriptions);
	}


	@Test
	public void allReadingsRemainAvailableForFiltering() {
		TestPredictions predictions = new TestPredictions();
		predictions.receive("LV绿", "LU绿");
		predictions.stripTranscriptions();

		predictions.stripNativeWords();

		assertEquals(Arrays.asList("LU", "LV"), predictions.getList());
		assertEquals("LV", predictions.getTranscription("绿"));
	}


	@Test
	public void acceptingAnUnfilteredWordLearnsItsFirstRankedReading() throws InvalidLanguageCharactersException {
		TestPredictions predictions = new TestPredictions();
		predictions.receive("LV绿", "LU绿");
		predictions.stripTranscriptions();

		predictions.onAcceptIdeogram("绿");

		assertEquals("LV绿", predictions.learnedWord);
		assertEquals("58", predictions.learnedSequence);
	}


	@Test
	public void acceptingAFilteredWordLearnsTheFilteredReading() throws InvalidLanguageCharactersException {
		TestPredictions predictions = new TestPredictions();
		predictions.receive("LV绿", "LU绿");
		predictions.stripNativeWords();
		// The filtered database query returns only entries matching the selected LU reading.
		predictions.receive("LU绿", "LU路");
		predictions.stripTranscriptions();

		predictions.onAcceptIdeogram("绿");

		assertEquals("LU绿", predictions.learnedWord);
		assertEquals("58", predictions.learnedSequence);
	}


	private static class TestPredictions extends IdeogramPredictions {
		String learnedWord;
		String learnedSequence;

		TestPredictions() {
			super(null);
			setLanguage(new TestChinese());
			setDigitSequence("58");
		}

		void receive(String... entries) {
			onDbWords(new ArrayList<>(Arrays.asList(entries)), false);
		}

		@Override protected void pairWithPreviousWord(String word, String sequence) {}

		@Override protected void makeTopWord(String word, String sequence) {
			learnedWord = word;
			learnedSequence = sequence;
		}
	}


	private static class TestChinese extends NullLanguage {
		TestChinese() {
			id = 462106;
			hasTranscriptionsEmbedded = true;
		}
	}
}
