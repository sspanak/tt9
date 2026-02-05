package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;

class MindReaderNgram implements Comparable<MindReaderNgram> {
	final long before;
	final int next;
	final long complete;
	final boolean isValid;
	final int size;

	MindReaderNgram(int[] tokens) {
		size = tokens.length;

		if (tokens.length == 0) {
			before = -1;
			next = -1;
			complete = -1;
			isValid = false;
		} else if (tokens.length == 1) {
			before = tokens[0];
			next = -1;
			complete = tokens[0];
			isValid = validate(tokens);
		} else {
			before = compressBefore(tokens);
			next = tokens[tokens.length - 1];
			complete = compressComplete(tokens);
			isValid = validate(tokens);
		}
	}

	private long compressBefore(int[] tokens) {
		long compressed = 0;
		for (int i = tokens.length - 2; i >= 0; i--) {
			compressed = compressed | ((long) tokens[i] << i * MindReader.DICTIONARY_WORD_SIZE);
		}
		return compressed;
	}

	private long compressComplete(int[] tokens) {
		long compressed = 0;
		for (int i = tokens.length - 1; i >= 0; i--) {
			compressed = compressed | ((long) tokens[i] << i * MindReader.DICTIONARY_WORD_SIZE);
		}
		return compressed;
	}

	private boolean validate(int[] tokens) {
		if (tokens.length == 0 || MindReaderDictionary.isNumber(tokens[0])) {
			return false;
		}

		final int HALF_SIZE = Math.round((float) tokens.length / 2);
		int garbage = 0;
		int emojis = 0;
		int numbers = 0;
		int punctuation = 0;
		int words = 0;

		for (int token : tokens) {
			if (MindReaderDictionary.isEmoji(token)) {
				emojis++;
			} else if (MindReaderDictionary.isGarbage(token)) {
				garbage++;
			} else if (MindReaderDictionary.isNumber(token)) {
				numbers++;
			} else if (MindReaderDictionary.isPunctuation(token)) {
				punctuation++;
			} else {
				words++;
			}
		}

		return
			garbage < HALF_SIZE
			&& emojis < HALF_SIZE
			&& numbers < HALF_SIZE
			&& punctuation <= HALF_SIZE
			&& words >= (numbers + emojis);
	}


	@Override
	public int compareTo(MindReaderNgram other) {
		if (other == null || other.before < before) {
			return 1;
		} else if (other.before > before) {
			return -1;
		} else {
			return 0;
		}
	}


	@NonNull
	@Override
	public String toString() {
		return "MindReaderNgram{" +
			"before=" + before +
			", next=" + next +
			", complete=" + complete +
			", isValid=" + isValid +
			", size=" + size +
			'}';
	}
}
