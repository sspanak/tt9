package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;

class MindReaderNgramList {
	private long[] before;
	private int[] next;
	private final int initialCapacity;
	private int size;


	MindReaderNgramList(int capacity) {
		before = new long[capacity];
		next = new int[capacity];
		initialCapacity = capacity;
		size = 0;
	}


	void add(@NonNull MindReaderNgram ngram) {
		if (!ngram.isValid || contains(ngram)) {
			return;
		}

		if (size >= before.length) {
			expand();
		}

		before[size] = ngram.before;
		next[size] = ngram.next;
		size++;
	}


	void addMany(@NonNull MindReaderNgram[] ngrams) {
		for (MindReaderNgram ngram : ngrams) {
			add(ngram);
		}
	}


	boolean contains(@NonNull MindReaderNgram ngram) {
		return indexOf(ngram) != -1;
	}


	int indexOf(@NonNull MindReaderNgram ngram) {
		for (int i = 0; i < size; i++) {
			if (before[i] == ngram.before && next[i] == ngram.next) {
				return i;
			}
		}

		return -1;
	}


	private void expand() {
		long[] newBeforeStorage = new long[before.length + initialCapacity];
		int[] newNextStorage = new int[next.length + initialCapacity];

		System.arraycopy(before, 0, newBeforeStorage, 0, before.length);
		System.arraycopy(next, 0, newNextStorage, 0, next.length);

		before = newBeforeStorage;
		next = newNextStorage;
	}


	@Override
	@NonNull
	public String toString() {
		final StringBuilder str = new StringBuilder();

		for (int i = 0; i < size; i++) {
			str.append("{").append(before[i]).append(", ").append(next[i]).append("}, ");
		}

		return str.substring(0, Math.max(0, str.length() - 2));
	}
}
