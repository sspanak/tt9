package io.github.sspanak.tt9.db.mindReading;

class MindReaderNgram {
	final long before;
	final int next;
	final long complete;
	final boolean isValid;
	final boolean isUnigram;

	MindReaderNgram(int[] tokens) {
		if (tokens.length == 0) {
			before = -1;
			next = -1;
			complete = -1;
			isValid = false;
			isUnigram = true;
		} else if (tokens.length == 1) {
			before = tokens[0];
			next = -1;
			complete = tokens[0];
			isValid = validate(tokens);
			isUnigram = true;
		} else {
			before = compressBefore(tokens);
			next = tokens[tokens.length - 1];
			complete = compressComplete(tokens);
			isValid = validate(tokens);
			isUnigram = false;
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
}
