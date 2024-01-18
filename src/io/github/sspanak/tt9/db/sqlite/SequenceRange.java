package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

class SequenceRange {
	public String sequence;
	public int start;
	public int end;

	public static SequenceRange create(@NonNull String sequence, int start) {
		SequenceRange range = new SequenceRange();
		range.sequence = sequence;
		range.start = start;

		return range;
	}

	public void endAt(int position) {
		end = position;
	}

	@NonNull
	@Override
	public String toString() {
		return sequence + " -> " + " [" + start + ", " + end + "]";
	}
}
