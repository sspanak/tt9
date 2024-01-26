package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;

public class WordBatch {
	@NonNull private final Language language;
	@NonNull private final ArrayList<Word> words = new ArrayList<>();
	@NonNull private final ArrayList<WordPosition> positions = new ArrayList<>();

	private WordPosition lastWordPosition;
	private int maxPositionRange;

	public WordBatch(@NonNull Language language) {
		this.language = language;
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
			maxPositionRange = Math.max(maxPositionRange, lastWordPosition.getRangeLength());

			lastWordPosition = WordPosition.create(sequence, position);
		}
	}

	public int getMaxPositionRange() {
		return maxPositionRange;
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
