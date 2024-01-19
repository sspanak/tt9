package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

class WordPosition {
	public String sequence;
	public int start;
	public int end;

	public static WordPosition create(@NonNull String sequence, int start) {
		WordPosition position = new WordPosition();
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
