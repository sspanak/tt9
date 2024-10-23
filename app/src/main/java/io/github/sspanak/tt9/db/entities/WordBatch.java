package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.languages.Language;

public class WordBatch {
	@NonNull private final Language language;
	@NonNull private final ArrayList<Word> words;
	@NonNull private final ArrayList<WordPosition> positions;

	public WordBatch(@NonNull Language language, int size) {
		this.language = language;
		words = size > 0 ? new ArrayList<>(size) : new ArrayList<>();
		positions = size > 0 ? new ArrayList<>(size) : new ArrayList<>();
	}

	public WordBatch(@NonNull Language language) {
		this(language, 0);
	}

	public void add(String word, int frequency, int position) throws InvalidLanguageCharactersException {
		words.add(Word.create(word, frequency, position));
		positions.add(WordPosition.create(language.getDigitSequenceForWord(word), position, position));
	}

	public void add(@NonNull WordFileLine line, int position) {
		words.addAll(Word.create(line, position));

		if (position == 0) {
			return;
		}

		WordPosition.create(line.digitSequence, position, position + line.words.size() - 1);
	}

	public void clear() {
		words.clear();
		positions.clear();
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
