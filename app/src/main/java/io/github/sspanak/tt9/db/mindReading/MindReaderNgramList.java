package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

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
		// @todo: also allow at most 4 next variations per before. New variations override oldest ones.
		// @todo: come up with a frequency count mechanism
		if (!ngram.isValid || ngram.isUnigram || contains(ngram)) {
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


	Set<Integer> getAllNextTokens(MindReaderContext current) {
		final Set<Integer> results = new HashSet<>();

		for (int i = 0; i < size; i++) {
			for (MindReaderNgram currentNgram : current.getEndingNgrams()) {
				if (currentNgram.complete == before[i]) {
					results.add(next[i]);
				}
			}
		}

		// @todo: sort by before length descending
		// @todo: sort by frequency if we implement that

		return results;
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
