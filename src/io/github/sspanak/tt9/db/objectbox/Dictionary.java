package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;

public class Dictionary {
	@NonNull private final Language language;
	@NonNull private final WordList words = new WordList();

	@NonNull private final SequenceIndex index;

	public Dictionary(@NonNull Language language) {
		this.language = language;
		index = new SequenceIndex(language);
	}

	/**
	 * Adds a word and a digit sequence to the end of the internal lists, assuming this is called
	 * repeatedly with properly sorted data.
	 */
	public void append(@NonNull String word, int frequency) throws InvalidLanguageCharactersException {
		int position = words.size();

		index.append(language.getDigitSequenceForWord(word), position);
		words.add(Word.create(language, word, frequency, position));
	}

	/**
	 * Adds a single word at the appropriate position in the list and in the index. Use this for
	 * adding a single word or a small amount of unordered words.
	 */
	public void add(@NonNull String word, int frequency, boolean custom) throws InvalidLanguageCharactersException {
		String sequence = language.getDigitSequenceForWord(word);
		int position = findPosition(word, sequence);
//		words.add(position, Word.create(language, word, frequency, position, custom));
		// @todo: index.add(sequence);
	}

	public void add(@NonNull String word, int frequency) throws InvalidLanguageCharactersException {
		add(word, frequency, false);
	}

	@NonNull public WordList find(String sequence) {
		SequenceRange range = index.find(sequence);
		return range != null ? new WordList(words.subList(range.start, range.end + 1)) : new WordList();
	}

	public void save() {

	}

	private int findPosition(@NonNull String word, @NonNull String sequence) {
		// @todo: Implement ...
		return 0;
	}

	@NonNull
	@Override
	public String toString() {
		final int MAX_ITEMS = 15;

		return " === " + getClass().getSimpleName() + " contents === " +
			"\nWords:\n" + words.toDebugString(MAX_ITEMS) +
			"\nIndex:\n" + index.toDebugString(MAX_ITEMS) ;
	}
}
