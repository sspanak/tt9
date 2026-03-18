package io.github.sspanak.tt9.db.mindReading;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import io.github.sspanak.tt9.preferences.settings.SettingsStatic;

class MindReaderNgramList {
	private final int[] MAX_NGRAM_VARIATIONS;

	private long[] before;
	private int[] next;
	private final HashMap<Long, Integer> index = new HashMap<>();
	private final int initialCapacity;
	private int size;


	MindReaderNgramList() {
		MAX_NGRAM_VARIATIONS = new int[] {
			SettingsStatic.MIND_READER_MAX_BIGRAM_SUGGESTIONS,
			SettingsStatic.MIND_READER_MAX_TRIGRAM_SUGGESTIONS,
			SettingsStatic.MIND_READER_MAX_TETRAGRAM_SUGGESTIONS
		};

		initialCapacity = SettingsStatic.MIND_READER_NGRAMS_INITIAL_CAPACITY;
		before = new long[initialCapacity];
		next = new int[initialCapacity];
		size = 0;
	}


	void add(@NonNull MindReaderNgram ngram) {
		if (!ngram.isValid || ngram.size < 2) {
			return;
		}

		// if we re-insert an N-gram, move it to the end to mark it as most recently used
		final int idx = indexOf(ngram);
		if (idx != -1) {
			moveToEnd(idx);
			return;
		}

		if (size >= before.length) {
			expand();
		}

		before[size] = ngram.before;
		next[size] = ngram.next;
		index.put(getIndex(ngram.before, ngram.next), size);
		size++;

		// keep only the most recent N-gram variations for a given context,
		// to prevent the list from growing indefinitely and to keep the predictions relevant
		removeOldestVariations(ngram, MAX_NGRAM_VARIATIONS[Math.min(MAX_NGRAM_VARIATIONS.length - 1, ngram.size - 2)]);
	}


	private void expand() {
		long[] newBeforeStorage = new long[before.length + initialCapacity];
		int[] newNextStorage = new int[next.length + initialCapacity];

		System.arraycopy(before, 0, newBeforeStorage, 0, before.length);
		System.arraycopy(next, 0, newNextStorage, 0, next.length);

		before = newBeforeStorage;
		next = newNextStorage;
	}


	private int indexOf(@NonNull MindReaderNgram ngram) {
		Integer idx = index.get(getIndex(ngram.before, ngram.next));
		return idx != null ? idx : -1;
	}


	private static long getIndex(long before, int next) {
		final long mask = (1L << SettingsStatic.MIND_READER_DICTIONARY_WORD_SIZE) - 1L;
		final long maskedNext = ((long) next) & mask;
		return (before << SettingsStatic.MIND_READER_DICTIONARY_WORD_SIZE) | maskedNext;
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


	private void moveToEnd(int position) {
		if (position < 0 || position >= size) {
			return;
		}

		long beforeValue = before[position];
		int nextValue = next[position];

		// shift elements one position to the left
		System.arraycopy(before, position + 1, before, position, size - position - 1);
		System.arraycopy(next, position + 1, next, position, size - position - 1);

		for (int i = position; i < size - 1; i++) {
			index.put(getIndex(before[i], next[i]), i);
		}

		// place the target element at the end
		before[size - 1] = beforeValue;
		next[size - 1] = nextValue;
		index.put(getIndex(beforeValue, nextValue), size - 1);
	}


	private void removeAt(int position) {
		if (position < 0 || position >= size) {
			return;
		}

		// remove the target element
		long beforeValue = before[position];
		int nextValue = next[position];
		index.remove(getIndex(beforeValue, nextValue));

		// shift all following elements one position to the left.
		System.arraycopy(before, position + 1, before, position, size - position - 1);
		System.arraycopy(next, position + 1, next, position, size - position - 1);

		for (int i = position; i < size - 1; i++) {
			index.put(getIndex(before[i], next[i]), i);
		}

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
