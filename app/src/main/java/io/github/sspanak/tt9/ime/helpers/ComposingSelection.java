package io.github.sspanak.tt9.ime.helpers;

import java.util.ArrayDeque;
import java.util.Iterator;

/** Tracks absolute composing bounds and the selection callbacks caused by an IME edit. */
final class ComposingSelection {
	private static final int MAX_EXPECTED_SELECTIONS = 64;
	private static final long CALLBACK_TIMEOUT = 2000;
	private final ArrayDeque<ExpectedSelection> expected = new ArrayDeque<>();
	private int start;
	private int end;
	private int composingStart = -1;
	private int composingEnd = -1;
	private int reportedStart;
	private int reportedEnd;
	private long revision;
	private long acknowledgedRevision;
	private boolean trackingCursor;


	ComposingSelection(int start, int end) {
		this.start = start;
		this.end = end;
		reportedStart = start;
		reportedEnd = end;
	}


	int getStart() { return start; }
	int getEnd() { return end; }
	int getComposingStart() { return composingStart >= 0 ? composingStart : Math.min(start, end); }


	/** Predict a replacement before its asynchronous callback, including committed text before it. */
	void replace(int length, int position, boolean track, long now) {
		int origin = getComposingStart();
		composingStart = length > 0 ? origin : -1;
		composingEnd = length > 0 ? origin + length : -1;
		start = end = Math.max(0, position > 0 ? origin + length + position - 1 : origin + position);
		expect(now);
		trackingCursor |= track;
	}


	void recompose(int lengthBeforeCursor, int length, long now) {
		composingStart = Math.max(0, Math.min(start, end) - lengthBeforeCursor);
		composingEnd = composingStart + length;
		start = end = composingEnd;
		expect(now);
	}


	void moveTo(int position, long now) {
		start = end = position;
		expect(now);
		trackingCursor = true;
	}


	void finish(long now) {
		composingStart = composingEnd = -1;
		if (!expected.isEmpty()) expect(now);
	}


	int getCursorOffset(int selectionStart, int selectionEnd, int candidatesStart, int candidatesEnd) {
		return selectionStart == selectionEnd && candidatesStart >= 0
			&& selectionStart >= candidatesStart && selectionStart <= candidatesEnd
			? selectionStart - candidatesStart : -1;
	}


	/**
	 * Keep the predicted latest position when an older edit's callback arrives. A genuinely new
	 * selection drops the history, so an outside tap is never pulled back into the composition.
	 */
	boolean update(int oldStart, int oldEnd, int selectionStart, int selectionEnd, int candidatesStart, int candidatesEnd, long now) {
		prune(now);
		for (Iterator<ExpectedSelection> it = expected.descendingIterator(); it.hasNext();) {
			ExpectedSelection selection = it.next();
			if (!selection.matches(selectionStart, selectionEnd, candidatesStart, candidatesEnd)) continue;
			// Once the latest write has been acknowledged, a new movement from that reported
			// position is a user action, even if its destination was an earlier editing cursor.
			if (selection.revision <= acknowledgedRevision && oldStart == reportedStart && oldEnd == reportedEnd
				&& candidatesStart == composingStart && candidatesEnd == composingEnd
				&& (selectionStart != start || selectionEnd != end)) break;
			if (selection.revision > acknowledgedRevision) {
				acknowledgedRevision = selection.revision;
				reportedStart = selectionStart;
				reportedEnd = selectionEnd;
			}
			return trackingCursor;
		}
		expected.clear();
		trackingCursor = false;
		start = selectionStart;
		end = selectionEnd;
		composingStart = candidatesStart;
		composingEnd = candidatesEnd;
		reportedStart = selectionStart;
		reportedEnd = selectionEnd;
		return false;
	}


	private void expect(long now) {
		prune(now);
		expected.addLast(new ExpectedSelection(start, end, composingStart, composingEnd, now, ++revision));
		while (expected.size() > MAX_EXPECTED_SELECTIONS) expected.removeFirst();
	}


	private void prune(long now) {
		while (!expected.isEmpty() && now - expected.getFirst().time > CALLBACK_TIMEOUT) expected.removeFirst();
		if (expected.isEmpty()) trackingCursor = false;
	}


	private static final class ExpectedSelection {
		private final int start;
		private final int end;
		private final int composingStart;
		private final int composingEnd;
		private final long time;
		private final long revision;

		ExpectedSelection(int start, int end, int composingStart, int composingEnd, long time, long revision) {
			this.start = start;
			this.end = end;
			this.composingStart = composingStart;
			this.composingEnd = composingEnd;
			this.time = time;
			this.revision = revision;
		}

		boolean matches(int start, int end, int composingStart, int composingEnd) {
			return this.start == start && this.end == end
				&& this.composingStart == composingStart && this.composingEnd == composingEnd;
		}
	}
}
