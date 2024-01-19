package io.github.sspanak.tt9.db.sqlite;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class DictionaryWordBatch {
	private final int MAX_SIZE;
	private boolean isFull;
	private final Language language;
	private WordPosition lastWordPosition;
	@NonNull public final ArrayList<Word> words = new ArrayList<>();
	@NonNull public final ArrayList<WordPosition> wordPositions = new ArrayList<>();


	public DictionaryWordBatch(@NonNull Language language, @NonNull SettingsStore settings) {
		this.language = language;
		MAX_SIZE = settings.getDictionaryImportWordChunkSize();
	}

	/**
	 * Adds a word and a digit sequence to the end of the internal lists, assuming this is called
	 * repeatedly with properly sorted data.
	 * <p>
	 * When the batch becomes full, this will refuse to add more words and return false. In this case,
	 * you must call save() to store the words in the database and clear the batch.
	 */
	public boolean add(@NonNull String word, short frequency, int position) throws InvalidLanguageCharactersException {
		if (isFull) {
			return false;
		}

		words.add(Word.create(word, frequency, position));
		String sequence = language.getDigitSequenceForWord(word);
		if (position == 0) {
			return true;
		}

		if (position == 1 || lastWordPosition == null) {
			lastWordPosition = WordPosition.create(sequence, position);
		}

		if (!sequence.equals(lastWordPosition.sequence)) {
			lastWordPosition.endAt(position - 1);

			isFull = wordPositions.size() >= MAX_SIZE;
			if (!isFull) {
				wordPositions.add(lastWordPosition);
				lastWordPosition = WordPosition.create(sequence, position);
			}
		}

		return !isFull;
	}

	public void clear() {
		isFull = false;
		lastWordPosition = null;
		words.clear();
		wordPositions.clear();
	}

	public void save() {
		DictionaryDb.upsertWordsSync(language, this);
		clear();
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
			"\nIndex:\n" + elementToDebugString(wordPositions, MAX_ITEMS) ;
	}
}
