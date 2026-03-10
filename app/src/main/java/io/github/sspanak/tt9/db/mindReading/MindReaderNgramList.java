package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;

class MindReaderNgramList {
	private final int[] MAX_NGRAM_VARIATIONS;

	private long[] before;
	private int[] next;
	private final int initialCapacity;
	private int size;


	MindReaderNgramList(int capacity, int maxBigramVariations, int maxTrigramVariations, int maxTetragramVariations) {
		MAX_NGRAM_VARIATIONS = new int[] {maxBigramVariations, maxTrigramVariations, maxTetragramVariations};

		before = new long[capacity];
		next = new int[capacity];
		initialCapacity = capacity;
		size = 0;
	}


	void add(@NonNull MindReaderNgram ngram) {
		if (!ngram.isValid || ngram.size < 2) {
			return;
		}

		// if we re-insert an N-gram, move it to the end to mark it as most recently used
		final int index = indexOf(ngram);
		if (index != -1) {
			moveToEnd(index);
			return;
		}

		if (size >= before.length) {
			expand();
		}

		before[size] = ngram.before;
		next[size] = ngram.next;
		size++;

		// keep only the most recent N-gram variations for a given context,
		// to prevent the list from growing indefinitely and to keep the predictions relevant
		removeOldestVariations(ngram, MAX_NGRAM_VARIATIONS[Math.min(4, ngram.size - 2)]);
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


	@NonNull
	Set<Integer> getNextTokens(@NonNull MindReaderDictionary dictionary, @NonNull MindReaderContext current) {
		final MindReaderNgram currentNgram = current.toEndingNgram(dictionary);
		final int maxIndex = Math.min(MAX_NGRAM_VARIATIONS.length - 1, Math.max(currentNgram.size - 2, 0));
		final Set<Integer> results = new LinkedHashSet<>(MAX_NGRAM_VARIATIONS[maxIndex]);

		// We want to show more recent first, so we loop from the end to the beginning.
		for (int i = size - 1; i >= 0; i--) {
			if (currentNgram.complete == before[i]) {
				results.add(next[i]);
			}

			if (results.size() >= MAX_NGRAM_VARIATIONS[maxIndex]) {
				break;
			}
		}

		return results;
	}


	private void moveToEnd(int index) {
		if (index < 0 || index >= size) {
			return;
		}

		long beforeValue = before[index];
		int nextValue = next[index];

		System.arraycopy(before, index + 1, before, index, size - index - 1);
		System.arraycopy(next, index + 1, next, index, size - index - 1);

		before[size - 1] = beforeValue;
		next[size - 1] = nextValue;
	}


	private void removeAt(int index) {
		if (index < 0 || index >= size) {
			return;
		}

		System.arraycopy(before, index + 1, before, index, size - index - 1);
		System.arraycopy(next, index + 1, next, index, size - index - 1);

		size--;
	}


	private void removeOldestVariations(@NonNull MindReaderNgram ngram, int maxVariations) {
		for (int i = size - 1, variations = 0; i >= 0; i--) {
			if (before[i] == ngram.before) {
				if (variations < maxVariations) {
					variations++;
				} else {
					removeAt(i);
				}
			}
		}
	}


	int size() {
		return size;
	}


	int capacity() {
		return before.length;
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
