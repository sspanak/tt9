package io.github.sspanak.tt9.ime.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ComposingSelectionTest {
	@Test
	public void nativeCursorKeepsTheCommittedPrefixWhenReplacingHanWithPinyin() {
		ComposingSelection selection = new ComposingSelection(12, 12);
		selection.replace(1, 1, false, 0);
		selection.update(12, 12, 13, 13, 12, 13, 1);
		selection.replace(4, 1, true, 2);
		selection.moveTo(14, 2);
		assertEquals(12, selection.getComposingStart());
		assertEquals(14, selection.getStart());
		assertTrue(selection.update(13, 13, 14, 14, 12, 16, 3));
	}


	@Test
	public void staleCallbackDoesNotMoveThePredictedLatestCursor() {
		ComposingSelection selection = new ComposingSelection(8, 8);
		selection.replace(4, 1, true, 0);
		selection.moveTo(10, 0);
		selection.replace(5, 1, true, 1);
		selection.moveTo(11, 1);
		assertTrue(selection.update(8, 8, 11, 11, 8, 13, 2));
		assertTrue(selection.update(8, 8, 10, 10, 8, 12, 3));
		assertTrue(selection.update(11, 11, 10, 10, 8, 12, 4));
		assertEquals(11, selection.getStart());
		assertEquals(8, selection.getComposingStart());
	}


	@Test
	public void pendingNormalComposingCallbackCannotUndoTheNewPinyinCursor() {
		ComposingSelection selection = new ComposingSelection(8, 8);
		selection.replace(1, 1, false, 0);
		selection.replace(4, 1, true, 1);
		selection.moveTo(10, 1);
		assertTrue(selection.update(8, 8, 9, 9, 8, 9, 2));
		assertEquals(10, selection.getStart());
		assertEquals(8, selection.getComposingStart());
	}


	@Test
	public void recomposingACommittedWordTracksItsStartBeforeAnyCallback() {
		ComposingSelection selection = new ComposingSelection(14, 14);
		selection.recompose(4, 6, 0);
		selection.replace(6, 1, true, 1);
		selection.moveTo(12, 1);
		assertEquals(10, selection.getComposingStart());
		assertTrue(selection.update(14, 14, 12, 12, 10, 16, 2));
	}


	@Test
	public void touchingAnEarlierCursorPositionAfterTheLatestCallbackIsAUserMove() {
		ComposingSelection selection = new ComposingSelection(0, 0);
		selection.replace(4, 1, true, 0);
		selection.moveTo(3, 0);
		assertTrue(selection.update(0, 0, 3, 3, 0, 4, 1));
		selection.replace(4, 1, true, 2);
		selection.moveTo(2, 2);
		assertTrue(selection.update(3, 3, 2, 2, 0, 4, 3));
		assertFalse(selection.update(2, 2, 3, 3, 0, 4, 4));
		assertEquals(3, selection.getStart());
	}


	@Test
	public void outsideSelectionIsNotSwallowedAndDropsOldExpectations() {
		ComposingSelection selection = new ComposingSelection(10, 10);
		selection.replace(4, 1, true, 0);
		selection.moveTo(12, 0);
		assertFalse(selection.update(12, 12, 3, 3, 10, 14, 1));
		assertEquals(3, selection.getStart());
		assertEquals(-1, selection.getCursorOffset(3, 3, 10, 14));
		assertFalse(selection.update(3, 3, 12, 12, 10, 14, 2));
	}


	@Test
	public void selectedTextUsesItsStartAsTheReplacementOrigin() {
		ComposingSelection selection = new ComposingSelection(9, 4);
		selection.replace(4, 1, true, 0);
		selection.moveTo(6, 0);
		assertEquals(4, selection.getComposingStart());
		assertTrue(selection.update(9, 4, 6, 6, 4, 8, 1));
		assertEquals(2, selection.getCursorOffset(6, 6, 4, 8));
		assertEquals(-1, selection.getCursorOffset(5, 7, 4, 8));
	}


	@Test
	public void emptyReadingKeepsTheCaretAfterTheCommittedPrefix() {
		ComposingSelection selection = new ComposingSelection(7, 7);
		selection.replace(1, 1, true, 0);
		selection.replace(0, 1, true, 1);
		selection.moveTo(7, 1);
		assertTrue(selection.update(8, 8, 7, 7, -1, -1, 2));
		assertEquals(7, selection.getComposingStart());
	}


	@Test
	public void conversionCanReplaceTheReadingBeforeItsLastCursorCallbackArrives() {
		ComposingSelection selection = new ComposingSelection(6, 6);
		selection.replace(4, 1, true, 0);
		selection.moveTo(8, 0);
		selection.replace(1, 1, false, 1);
		assertTrue(selection.update(6, 6, 7, 7, 6, 7, 2));
		assertTrue(selection.update(6, 6, 8, 8, 6, 10, 3));
		assertEquals(7, selection.getStart());
	}


	@Test
	public void completedCompositionAndExpiredCallbacksDoNotTrapTheNextSelection() {
		ComposingSelection selection = new ComposingSelection(0, 0);
		selection.replace(4, 1, true, 0);
		selection.moveTo(2, 0);
		selection.finish(1);
		assertTrue(selection.update(0, 0, 2, 2, -1, -1, 2));
		assertFalse(selection.update(2, 2, 4, 4, 0, 4, 2100));
	}


	@Test
	public void aLongHeldArrowHasBoundedCallbackHistory() {
		ComposingSelection selection = new ComposingSelection(0, 0);
		selection.replace(200, 1, true, 0);
		for (int i = 1; i <= 100; i++) selection.moveTo(i, i);
		assertFalse(selection.update(0, 0, 1, 1, 0, 200, 101));
	}
}
