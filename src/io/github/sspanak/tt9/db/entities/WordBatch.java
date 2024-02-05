package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;

public class WordBatch {
	@NonNull private final Language language;
	@NonNull private final ArrayList<Word> words;
	@NonNull private final ArrayList<WordPosition> positions;

	private WordPosition lastWordPosition;

	public WordBatch(@NonNull Language language, int size) {
		this.language = language;
		words = size > 0 ? new ArrayList<>(size) : new ArrayList<>();
		positions = size > 0 ? new ArrayList<>(size) : new ArrayList<>();
	}

	public WordBatch(@NonNull Language language) {
		this(language, 0);
	}

	public void add(@NonNull String word, int frequency, int position) throws InvalidLanguageCharactersException {
		words.add(Word.create(word, frequency, position));

		if (position == 0) {
			return;
		}

		String sequence = language.getDigitSequenceForWord(word);

		if (position == 1 || lastWordPosition == null) {
			lastWordPosition = WordPosition.create(sequence, position);
		} else {
			lastWordPosition.end = position;
		}

		if (!sequence.equals(lastWordPosition.sequence)) {
			lastWordPosition.end--;
			positions.add(lastWordPosition);

			lastWordPosition = WordPosition.create(sequence, position);
		}
	}

	@NonNull public Language getLanguage() {
		return language;
	}

	@NonNull public ArrayList<Word> getWords() {
		return words;
	}

	@NonNull public ArrayList<WordPosition> getPositions() {
		return positions;
	}
}
