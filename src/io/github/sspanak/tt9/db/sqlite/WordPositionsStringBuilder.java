package io.github.sspanak.tt9.db.sqlite;

import android.database.Cursor;

import androidx.annotation.NonNull;


public class WordPositionsStringBuilder {
	int size = 0;
	StringBuilder positions = new StringBuilder();


	public static WordPositionsStringBuilder fromDbRanges(Cursor cursor) {
		WordPositionsStringBuilder builder = new WordPositionsStringBuilder();
		while (cursor.moveToNext()) {
			builder.append(cursor.getInt(0), cursor.getInt(1));
		}

		return builder;
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
