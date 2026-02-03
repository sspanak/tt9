package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.util.Logger;

public class MindReaderContext {
	private static final String LOG_TAG = MindReaderContext.class.getSimpleName();

	private String mockContext;
	private final String[] buffer = new String[4]; // @todo: convert this to int
	private int size = 0;


	boolean isEmpty() {
		return mockContext == null || mockContext.isEmpty();
	}

	public boolean set(@Nullable String beforeCursor) {
		final boolean isBeforeEmpty = beforeCursor == null || beforeCursor.isEmpty();
		if (isEmpty() && isBeforeEmpty) {
			return false;
		}

		mockContext = beforeCursor;
		Logger.d(LOG_TAG, "========> set context: " + this);

		return true;
	}

//	public void add(@Nullable String word) {
//		if (word == null || word.isEmpty()) {
//			Logger.d(LOG_TAG, "Not adding an empty word to the MindReader.");
//			return;
//		}
//
//		// @todo: convert the words to database positions.
//		//  - Allow only the Top64k words. The rest are noise.
//		//  - Punctuation IDs are hardcoded at ID > 10^7
//		if (size < 4) {
//			buffer[size++] = word;
//		} else {
//			buffer[0] = buffer[1];
//			buffer[1] = buffer[2];
//			buffer[2] = buffer[3];
//			buffer[3] = word;
//		}
//
//		Logger.d(LOG_TAG, "======> mind-reading context: " + this);
//	}

	@Override
	@NonNull
	public String toString() {
		return mockContext;
//		return java.util.Arrays.toString(buffer);
	}
}
