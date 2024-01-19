package io.github.sspanak.tt9.db.sqlite;

import android.database.Cursor;

import androidx.annotation.NonNull;


public class WordPositions {
	int size = 0;
	StringBuilder positions = new StringBuilder();


	public static WordPositions fromDbRanges(Cursor cursor) {
		WordPositions wordPositions = new WordPositions();
		while (cursor.moveToNext()) {
			wordPositions.add(cursor.getInt(0), cursor.getInt(1));
		}

		return wordPositions;
	}

	public void add(int start, int end) {
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
