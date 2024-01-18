package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;

public class DictionaryWordBatch {
	private final Language language;
	@NonNull public final ArrayList<Word> words = new ArrayList<>();
	@NonNull public final ArrayList<SequenceRange> sequences = new ArrayList<>();
	SequenceRange lastSequenceRange;


	public DictionaryWordBatch(@NonNull Language language) {
		this.language = language;
	}

	/**
	 * Adds a word and a digit sequence to the end of the internal lists, assuming this is called
	 * repeatedly with properly sorted data.
	 */
	public void add(@NonNull String word, short frequency, int position) throws InvalidLanguageCharactersException {
		words.add(Word.create(word, frequency, position));
		String sequence = language.getDigitSequenceForWord(word);
		if (position == 0) {
			return;
		}

		if (position == 1 || lastSequenceRange == null) {
			lastSequenceRange = SequenceRange.create(sequence, position);
		}

		if (!sequence.equals(lastSequenceRange.sequence)) {
			lastSequenceRange.endAt(position - 1);
			sequences.add(lastSequenceRange);
			lastSequenceRange = SequenceRange.create(sequence, position);
		}
	}

	public void clear() {
		words.clear();
		sequences.clear();
		lastSequenceRange = null;
	}

	public int size() {
		return words.size();
	}


	public String elementToDebugString(ArrayList element, int MAX_ITEMS) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < element.size() && i < MAX_ITEMS; i++) {
			sb
				.append(element.get(i).toString())
				.append("\n");
		}

		if (element.size() > MAX_ITEMS) {
			sb.append("...\n(Total: ").append(element.size()).append(")");
		}

		return sb.toString();
	}

	@NonNull
	@Override
	public String toString() {
		final int MAX_ITEMS = 15;

		return " === " + getClass().getSimpleName() + " contents === " +
			"\nWords:\n" + elementToDebugString(words, MAX_ITEMS) +
			"\nIndex:\n" + elementToDebugString(sequences, MAX_ITEMS) ;
	}
}
