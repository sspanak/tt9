package io.github.sspanak.tt9.db.entities;

import android.database.Cursor;

import androidx.annotation.NonNull;


public class WordPositionsStringBuilder {
	private int maxFuzzy = Integer.MAX_VALUE;
	private int size = 0;
	private final StringBuilder positions = new StringBuilder();

	public WordPositionsStringBuilder appendFromDbRanges(Cursor cursor) {
		while (cursor.moveToNext()) {
			append(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2) == 1);
		}

		return this;
	}

	private void append(int start, int end, boolean isExact) {
		if (size >= maxFuzzy && !isExact) {
			return;
		}

		if (size > 0) {
			positions.append(",");
		}
		positions.append(start);
		size++;

		for (int position = start + 1; position <= end && (size < maxFuzzy || isExact); position++) {
			positions.append(",").append(position);
			size++;
		}
	}

	public int getSize() {
		return size;
	}

	public WordPositionsStringBuilder setMaxFuzzy(int maxSize) {
		this.maxFuzzy = maxSize;
		return this;
	}

	@NonNull
	@Override
	public String toString() {
		return positions.toString();
	}
}
