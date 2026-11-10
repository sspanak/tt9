package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CandidateNumberingTest {
	@Test
	public void everyNaturalRowRestartsAtOneAndDigitsUseTheHighlightedRow() {
		CandidateNumbering numbering = new CandidateNumbering(new PackedCandidateNavigation(new int[] {30, 50, 40, 20, 70}, 100, 9));
		assertEquals(1, numbering.getNumber(0));
		assertEquals(2, numbering.getNumber(1));
		assertEquals(1, numbering.getNumber(2));
		assertEquals(2, numbering.getNumber(3));
		assertEquals(1, numbering.getNumber(4));
		assertEquals(0, numbering.getIndex(1, 1));
		assertEquals(2, numbering.getIndex(1, 3));
		assertEquals(3, numbering.getIndex(2, 2));
		assertEquals(4, numbering.getIndex(1, 4));
	}

	@Test
	public void movingRapidlyBetweenRowsKeepsLabelsAndShortcutsStable() {
		int[] widths = new int[30];
		java.util.Arrays.fill(widths, 10);
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(widths, 100, 9);
		CandidateNumbering numbering = new CandidateNumbering(navigation);
		int selected = 0;
		for (int repeat = 0; repeat < 30; repeat++) {
			selected = navigation.move(selected, 0, 1);
			assertEquals(9, selected);
			assertEquals(9, numbering.getIndex(1, selected));
			assertEquals(17, numbering.getIndex(9, selected));
			selected = navigation.move(selected, 0, -1);
			assertEquals(0, numbering.getIndex(1, selected));
			assertEquals(1, numbering.getNumber(9));
			assertEquals(9, numbering.getNumber(17));
		}
		assertEquals(27, numbering.getIndex(1, 29));
		assertEquals(29, numbering.getIndex(3, 27));
	}

	@Test
	public void shortRowsRejectDigitsBeyondTheirOwnEnd() {
		CandidateNumbering numbering = new CandidateNumbering(new PackedCandidateNavigation(new int[] {30, 50, 40, 20, 70}, 100, 9));
		assertEquals(-1, numbering.getIndex(3, 0));
		assertEquals(-1, numbering.getIndex(3, 2));
		assertEquals(-1, numbering.getIndex(2, 4));
		assertEquals(-1, numbering.getIndex(0, 0));
		assertEquals(-1, numbering.getIndex(10, 0));
	}

	@Test
	public void emptyListsAndStaleSelectionsCannotCommitAnotherRow() {
		CandidateNumbering empty = new CandidateNumbering(new PackedCandidateNavigation(null, 100, 9));
		assertEquals(-1, empty.getIndex(1, 0));
		assertEquals(0, empty.getNumber(0));
		CandidateNumbering numbering = new CandidateNumbering(new PackedCandidateNavigation(new int[] {40, 40}, 100, 9));
		assertEquals(-1, numbering.getIndex(1, -1));
		assertEquals(-1, numbering.getIndex(1, 2));
		assertEquals(0, numbering.getNumber(-1));
		assertEquals(1, numbering.getNumber(0));
		assertEquals(0, numbering.getNumber(2));
	}
}
