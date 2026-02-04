package io.github.sspanak.tt9.db.mindReading;

class MindReaderNgram {
	final long before;
	final int next;

	MindReaderNgram(int[] tokens) {
		if (tokens.length < 2) {
			before = -1;
			next = -1;
		} else {
			next = tokens[tokens.length - 1];

			long compactedBefore = 0;
			for (int i = tokens.length - 2; i >= 0; i--) {
				compactedBefore = compactedBefore | ((long) tokens[i] << i * MindReaderStore.DICTIONARY_WORD_SIZE);
			}

			before = compactedBefore;
		}
	}
}
