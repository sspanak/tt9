package io.github.sspanak.tt9.ime.modes.helpers;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PinyinSequenceEditorTest {
	@Test
	public void deletionWaitsForReadingLookupEvenWhenTheRemainingLettersMatchTheirDigits() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("9426", "XIAN");
		editor.setCursor(2);
		editor.deleteBeforeCursor();
		assertEquals("XAN", editor.getReading());
		assertFalse(editor.hasResolvedReading());
		editor.resolveReading(Collections.emptyList());
		assertFalse(editor.hasResolvedReading());
	}

	@Test
	public void aConfirmedReadingAfterDeletionCanConvertAgain() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("9426", "XIAN");
		editor.setCursor(3);
		editor.deleteBeforeCursor();
		assertEquals("XIN", editor.getReading());
		assertFalse(editor.hasResolvedReading());
		editor.resolveReading(Collections.singletonList("XIN"));
		assertTrue(editor.hasResolvedReading());
	}

	@Test
	public void nativeSelectionCanPlaceTheCursorWithoutChangingTheReading() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("94664", "ZHONG");
		editor.setCursor(1);
		assertEquals("Z|HONG", editor.getPreview());
		assertEquals("ZHONG", editor.getReading());
		assertTrue(editor.insertDigit(4));
		assertEquals("944664", editor.getDigits());
		assertEquals(2, editor.getCursor());
	}


	@Test
	public void nativeSelectionIsClampedToTheReadingBoundaries() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("64", "NI");
		editor.setCursor(-1);
		assertEquals(0, editor.getCursor());
		editor.setCursor(Integer.MAX_VALUE);
		assertEquals(2, editor.getCursor());
	}


	@Test
	public void selectedReadingStartsWithCursorAtItsEnd() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("9664", "zong");
		assertEquals("ZONG|", editor.getPreview());
		assertEquals(4, editor.getCursor());
		assertTrue(editor.hasResolvedReading());
	}


	@Test
	public void missingHCanBeInsertedWithoutRetypingZong() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("9664", "ZONG");
		editor.moveCursor(-1);
		editor.moveCursor(-1);
		editor.moveCursor(-1);
		assertEquals("Z|ONG", editor.getPreview());
		assertTrue(editor.insertDigit(4));
		assertEquals("94664", editor.getDigits());
		assertEquals("Z4|ONG", editor.getPreview());
		editor.resolveReading(Arrays.asList("XIONG", "ZHONG"));
		assertEquals("ZH|ONG", editor.getPreview());
		assertEquals("ZHONG", editor.getReading());
		assertTrue(editor.hasResolvedReading());
	}


	@Test
	public void deletingBeforeTheCursorPreservesTheRestOfTheReading() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("94664", "ZHONG");
		for (int i = 0; i < 3; i++) editor.moveCursor(-1);
		assertTrue(editor.deleteBeforeCursor());
		assertEquals("9664", editor.getDigits());
		assertEquals("Z|ONG", editor.getPreview());
	}


	@Test
	public void deletionAtTheLeftBoundaryDoesNothing() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("64", "NI");
		editor.moveCursor(-1);
		editor.moveCursor(-1);
		assertFalse(editor.deleteBeforeCursor());
		assertEquals("|NI", editor.getPreview());
		assertEquals("64", editor.getDigits());
	}


	@Test
	public void cursorDoesNotMovePastEitherEnd() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("64", "NI");
		editor.moveCursor(Integer.MAX_VALUE);
		assertEquals(2, editor.getCursor());
		for (int i = 0; i < 5; i++) editor.moveCursor(Integer.MIN_VALUE);
		assertEquals(0, editor.getCursor());
		editor.moveCursor(0);
		assertEquals("|NI", editor.getPreview());
	}


	@Test
	public void insertionWorksAtTheBeginningAndEnd() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("24", "AI");
		editor.moveCursor(-1);
		editor.moveCursor(-1);
		editor.insertDigit(9);
		assertEquals("924", editor.getDigits());
		assertEquals("9|AI", editor.getPreview());
		editor.moveCursor(1);
		editor.moveCursor(1);
		editor.insertDigit(6);
		assertEquals("9246", editor.getDigits());
		assertEquals("9AI6|", editor.getPreview());
	}


	@Test
	public void spaceAndPunctuationDigitsCannotEnterThePhoneticSequence() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("64", "NI");
		for (int digit : new int[] {-1, 0, 1, 10}) assertFalse(editor.insertDigit(digit));
		assertEquals("64", editor.getDigits());
		assertEquals("NI|", editor.getPreview());
	}


	@Test
	public void anUnmatchedInsertedDigitStaysVisibleUntilItCanBeResolved() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("64", "NI");
		editor.insertDigit(2);
		editor.resolveReading(Collections.emptyList());
		assertEquals("NI2|", editor.getPreview());
		assertFalse(editor.hasResolvedReading());
	}


	@Test
	public void staleResultsAndDifferentKeySequencesCannotReplaceThePreview() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("9664", "ZONG");
		editor.moveCursor(-1);
		editor.moveCursor(-1);
		editor.moveCursor(-1);
		editor.insertDigit(4);
		editor.resolveReading(Arrays.asList("ZONG", "ZANG", "ZHANG"));
		assertEquals("Z4|ONG", editor.getPreview());
	}


	@Test
	public void unchangedLettersArePreferredOverTheFirstDatabaseReading() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("64", "NI");
		editor.resolveReading(Arrays.asList("MI", "NI"));
		assertEquals("NI|", editor.getPreview());
	}


	@Test
	public void equallyMatchingReadingsKeepTheirDatabaseOrder() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("24", "AI");
		editor.moveCursor(-1);
		editor.moveCursor(-1);
		editor.insertDigit(9);
		editor.resolveReading(Arrays.asList("ZAI", "WAI"));
		assertEquals("Z|AI", editor.getPreview());
	}


	@Test
	public void allDigitsCanBeDeletedWithoutLosingTheVirtualEditor() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("64", "NI");
		assertTrue(editor.deleteBeforeCursor());
		assertTrue(editor.deleteBeforeCursor());
		assertFalse(editor.deleteBeforeCursor());
		assertEquals("", editor.getDigits());
		assertEquals("|", editor.getPreview());
		assertFalse(editor.hasResolvedReading());
		editor.insertDigit(2);
		editor.resolveReading(Collections.singletonList("A"));
		assertEquals("A|", editor.getPreview());
	}


	@Test
	public void umlautKeepsTheExistingVSpelling() {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("58", "lv");
		assertEquals("LV|", editor.getPreview());
		assertTrue(editor.hasResolvedReading());
	}


	@Test(expected = IllegalArgumentException.class)
	public void aReadingFromAnotherKeySequenceCannotStartEditing() {
		new PinyinSequenceEditor("9664", "ZHONG");
	}


	@Test
	public void asynchronousReadingResultsCanArriveWhileTheCursorIsEditing() throws Exception {
		PinyinSequenceEditor editor = new PinyinSequenceEditor("9664", "ZONG");
		FutureTask<Void> readingUpdates = new FutureTask<>(() -> {
			for (int i = 0; i < 2000; i++) {
				editor.resolveReading(Arrays.asList("ZONG", "ZHONG"));
				editor.getPreview();
			}
			return null;
		});
		new Thread(readingUpdates).start();
		for (int i = 0; i < 1000; i++) {
			editor.moveCursor(-1);
			editor.insertDigit(4);
			editor.deleteBeforeCursor();
			editor.moveCursor(1);
		}
		readingUpdates.get(10, TimeUnit.SECONDS);
		assertEquals("9664", editor.getDigits());
		assertEquals(4, editor.getCursor());
		assertEquals(5, editor.getPreview().length());
	}
}
