package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PackedCandidateNavigationTest {
	@Test
	public void numberedRowsStopAtNineWithoutStretchingTheirCandidates() {
		int[] widths = new int[20];
		java.util.Arrays.fill(widths, 10);
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(widths, 200, 9);
		assertEquals(3, navigation.getRowCount());
		assertEquals(0, navigation.getRow(8));
		assertEquals(1, navigation.getRow(9));
		assertEquals(1, navigation.getRow(17));
		assertEquals(2, navigation.getRow(18));
		assertEquals(120, navigation.getSpanSize(8));
		assertEquals(10, navigation.getItemWidth(8));
		assertEquals(17, navigation.move(8, 0, 1));
		assertEquals(8, navigation.move(17, 0, -1));
		assertEquals(19, navigation.move(17, 0, 1));
	}

	@Test
	public void allocatedSpansMatchNaturalAndCappedRowsForDifferentScreenWidths() {
		java.util.Random random = new java.util.Random(9);
		for (int width : new int[] {1, 100, 480, 1080, Integer.MAX_VALUE}) {
			for (int limit : new int[] {0, 1, 9}) {
				int[] widths = new int[100];
				for (int i = 0; i < widths.length; i++) widths[i] = 1 + random.nextInt(300);
				PackedCandidateNavigation navigation = new PackedCandidateNavigation(widths, width, limit);
				int used = 0;
				int row = 0;
				for (int i = 0; i < widths.length; i++) {
					int span = navigation.getSpanSize(i);
					if (span > width - used) {
						row++;
						used = 0;
					}
					assertEquals(row, navigation.getRow(i));
					assertEquals(Math.min(widths[i], width), navigation.getItemWidth(i));
					used += span;
				}
			}
		}
	}

	@Test
	public void disablingNumberingKeepsNaturalWidthsAndUnrestrictedRowLengths() {
		int[] widths = new int[20];
		java.util.Arrays.fill(widths, 10);
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(widths, 200, 0);
		assertEquals(1, navigation.getRowCount());
		assertEquals(10, navigation.getSpanSize(8));
		assertEquals(10, navigation.getItemWidth(8));
		assertEquals(20, navigation.getRowEnd(0));
	}

	@Test
	public void candidatesWrapByTheirIndividualWidths() {
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(new int[] {30, 50, 40, 20, 70}, 100);
		assertEquals(3, navigation.getRowCount());
		assertEquals(0, navigation.getRow(0));
		assertEquals(0, navigation.getRow(1));
		assertEquals(1, navigation.getRow(2));
		assertEquals(1, navigation.getRow(3));
		assertEquals(2, navigation.getRow(4));
		assertEquals(50, navigation.getSpanSize(1));
	}

	@Test
	public void exactFitsStayInOneRowAndOversizedWordsUseTheFullWidth() {
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(new int[] {30, 70, 150, 0, -4}, 100);
		assertEquals(3, navigation.getRowCount());
		assertEquals(0, navigation.getRow(1));
		assertEquals(1, navigation.getRow(2));
		assertEquals(2, navigation.getRow(3));
		assertEquals(100, navigation.getSpanSize(2));
		assertEquals(1, navigation.getSpanSize(3));
		assertEquals(1, navigation.getSpanSize(4));
	}

	@Test
	public void verticalMovesUseTheNearestHorizontalCenter() {
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(new int[] {20, 60, 20, 50, 50}, 100);
		assertEquals(3, navigation.move(0, 0, 1));
		assertEquals(3, navigation.move(1, 0, 1));
		assertEquals(4, navigation.move(2, 0, 1));
		assertEquals(0, navigation.move(3, 0, -1));
		assertEquals(2, navigation.move(4, 0, -1));
	}

	@Test
	public void partialLastRowAndTopRowDoNotWrapVertically() {
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(new int[] {30, 40, 30, 20}, 100);
		assertEquals(3, navigation.move(2, 0, 1));
		assertEquals(3, navigation.move(3, 0, 1));
		assertEquals(2, navigation.move(2, 0, -1));
	}

	@Test
	public void horizontalMovesFollowCandidateOrderAndClampAtBothEnds() {
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(new int[] {80, 30, 40}, 100);
		assertEquals(1, navigation.move(0, 1, 0));
		assertEquals(0, navigation.move(1, -1, 0));
		assertEquals(0, navigation.move(0, -1, 0));
		assertEquals(2, navigation.move(2, 1, 0));
		assertEquals(2, navigation.move(1, Integer.MAX_VALUE, -1));
	}

	@Test
	public void measurementsDoNotChangeWhenTheInputArrayOrVisibleViewsChange() {
		int[] widths = {40, 50, 80};
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(widths, 100);
		widths[0] = 100;
		assertEquals(40, navigation.getSpanSize(0));
		assertEquals(1, navigation.getRow(2));
		assertEquals(2, navigation.move(1, 0, 1));
		assertEquals(2, new PackedCandidateNavigation(new int[] {40, 50, 80}, 100).move(1, 0, 1));
	}

	@Test
	public void emptyAndInvalidIndicesHaveStableResults() {
		PackedCandidateNavigation empty = new PackedCandidateNavigation(new int[0], 100);
		assertEquals(0, empty.getRowCount());
		assertEquals(-1, empty.getRow(0));
		assertEquals(1, empty.getSpanSize(0));
		assertEquals(1, empty.getItemWidth(0));
		assertEquals(-1, empty.getRowStart(0));
		assertEquals(-1, empty.getRowEnd(0));
		assertEquals(0, empty.move(20, 1, 1));
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(new int[] {40, 40}, 100);
		assertEquals(-1, navigation.getRow(-1));
		assertEquals(-1, navigation.getRow(2));
		assertEquals(1, navigation.getSpanSize(-1));
		assertEquals(1, navigation.move(-10, 1, 0));
		assertEquals(0, navigation.move(10, -1, 0));
	}

	@Test
	public void invalidAvailableWidthsAndNullInputAreSafe() {
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(new int[] {30, 40}, 0);
		assertEquals(2, navigation.getRowCount());
		assertEquals(1, navigation.getSpanSize(0));
		assertEquals(0, new PackedCandidateNavigation(null, -1).getRowCount());
	}

	@Test
	public void largeWidthsDoNotOverflowPackingOrCenterDistances() {
		PackedCandidateNavigation navigation = new PackedCandidateNavigation(new int[] {Integer.MAX_VALUE - 1, 1, Integer.MAX_VALUE}, Integer.MAX_VALUE);
		assertEquals(2, navigation.getRowCount());
		assertEquals(0, navigation.getRow(1));
		assertEquals(2, navigation.move(1, 0, Integer.MAX_VALUE));
		assertEquals(0, navigation.move(2, 0, Integer.MIN_VALUE));
	}
}
