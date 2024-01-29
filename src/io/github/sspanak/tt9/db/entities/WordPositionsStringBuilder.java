package io.github.sspanak.tt9.db.entities;

import android.database.Cursor;

import androidx.annotation.NonNull;


public class WordPositionsStringBuilder {
	public int size = 0;
	StringBuilder positions = new StringBuilder();

	public WordPositionsStringBuilder appendFromDbRanges(Cursor cursor) {
		while (cursor.moveToNext()) {
			append(cursor.getInt(0), cursor.getInt(1));
		}

		return this;
	}

	private void append(int start, int end) {
		if (size > 0) {
			positions.append(",");
		}
		positions.append(start);
		size++;

		for (int position = start + 1; position <= end; position++) {
			positions.append(",").append(position);
			size++;
		}
	}

	public void clear() {
		size = 0;
		positions.setLength(0);
	}

	@NonNull
	@Override
	public String toString() {
		return positions.toString();
	}
}
