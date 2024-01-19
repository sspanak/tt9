package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

class DictionaryWordBatchPosition {
	public String sequence;
	public int start;
	public int end;

	public static DictionaryWordBatchPosition create(@NonNull String sequence, int start) {
		DictionaryWordBatchPosition position = new DictionaryWordBatchPosition();
		position.sequence = sequence;
		position.start = start;

		return position;
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
