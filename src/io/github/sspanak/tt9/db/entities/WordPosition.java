package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

public class WordPosition {
	public String sequence;
	public int start;
	public int end;

	public static WordPosition create(@NonNull String sequence, int start) {
		WordPosition position = new WordPosition();
		position.sequence = sequence;
		position.start = start;

		return position;
	}

	public static WordPosition create(@NonNull String sequence, int start, int end) {
		WordPosition position = create(sequence, start);
		position.end = end;

		return position;
	}

	public int getRangeLength() {
		return end - start + 1;
	}
}
