package io.github.sspanak.tt9.chinese;

import java.util.Arrays;

public final class PackedCandidateNavigation {
	private final int[] spans;
	private final int[] itemWidths;
	private final int[] rows;
	private final long[] centers;
	private final int[] rowStarts;

	public PackedCandidateNavigation(int[] widths, int availableWidth) {
		this(widths, availableWidth, 0);
	}

	public PackedCandidateNavigation(int[] widths, int availableWidth, int maxItemsPerRow) {
		int count = widths == null ? 0 : widths.length;
		int width = Math.max(1, availableWidth);
		int rowLimit = maxItemsPerRow > 0 ? maxItemsPerRow : Integer.MAX_VALUE;
		spans = new int[count];
		itemWidths = new int[count];
		rows = new int[count];
		centers = new long[count];
		int[] starts = new int[count];
		int row = 0;
		int used = 0;
		int rowItems = 0;
		for (int index = 0; index < count; index++) {
			int span = Math.max(1, Math.min(widths[index], width));
			if (span > width - used || rowItems == rowLimit) {
				// End a numbered row without stretching the last candidate's actual view.
				if (rowItems == rowLimit) spans[index - 1] += width - used;
				starts[++row] = index;
				used = 0;
				rowItems = 0;
			}
			spans[index] = span;
			itemWidths[index] = span;
			rows[index] = row;
			centers[index] = 2L * used + span;
			used += span;
			rowItems++;
		}
		rowStarts = Arrays.copyOf(starts, count == 0 ? 0 : row + 1);
	}

	public int getSpanSize(int index) {
		return index >= 0 && index < spans.length ? spans[index] : 1;
	}

	public int getItemWidth(int index) {
		return index >= 0 && index < itemWidths.length ? itemWidths[index] : 1;
	}

	public int getRowCount() {
		return rowStarts.length;
	}

	public int getRow(int index) {
		return index >= 0 && index < rows.length ? rows[index] : -1;
	}

	public int getRowStart(int row) {
		return row >= 0 && row < rowStarts.length ? rowStarts[row] : -1;
	}

	public int getRowEnd(int row) {
		return row < 0 || row >= rowStarts.length ? -1 : row + 1 < rowStarts.length ? rowStarts[row + 1] : spans.length;
	}

	public int move(int index, int horizontal, int vertical) {
		if (spans.length == 0) return 0;
		index = Math.max(0, Math.min(index, spans.length - 1));
		if (horizontal != 0) {
			return Math.max(0, Math.min(index + Integer.signum(horizontal), spans.length - 1));
		}
		int targetRow = rows[index] + Integer.signum(vertical);
		if (targetRow < 0 || targetRow >= rowStarts.length || vertical == 0) return index;

		int closest = rowStarts[targetRow];
		int end = targetRow + 1 < rowStarts.length ? rowStarts[targetRow + 1] : spans.length;
		long distance = Math.abs(centers[closest] - centers[index]);
		for (int candidate = closest + 1; candidate < end; candidate++) {
			long nextDistance = Math.abs(centers[candidate] - centers[index]);
			if (nextDistance < distance) {
				closest = candidate;
				distance = nextDistance;
			}
		}
		return closest;
	}
}
