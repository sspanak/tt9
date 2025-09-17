package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.util.TextTools;

public class CustomWord {
	public final NaturalLanguage language;
	public final String word;
	public final String sequence;

	public CustomWord(@NonNull String word, @NonNull String sequence, int langId) throws IllegalArgumentException {
		language = LanguageCollection.getLanguage(langId);
		if (language == null || word.isEmpty() || sequence.isEmpty()) {
			throw new IllegalArgumentException("Word, digit sequence and language must be provided.");
		}

		if (TextTools.containsPunctuation(word)) {
			throw new IllegalArgumentException("Custom word: '" + word + "' contains punctuation.");
		}

		this.sequence = sequence;
		this.word = word;
	}

	public CustomWord(@NonNull String word, NaturalLanguage language) throws InvalidLanguageCharactersException, IllegalArgumentException {
		if (word.isEmpty() || language == null) {
			throw new IllegalArgumentException("Word and language must be provided.");
		}

		this.word = word;
		this.language = language;
		this.sequence = language.getDigitSequenceForWord(word);

		if (TextTools.containsPunctuation(word)) {
			throw new IllegalArgumentException("Custom word: '" + word + "' contains punctuation.");
		}
	}

	@NonNull
	public static ArrayList<CustomWord> guessLanguage(@NonNull String word, @NonNull ArrayList<Integer> languageIds) {
		ArrayList<CustomWord> results = new ArrayList<>();

		for (Language lang : LanguageCollection.getAll(languageIds)) {
			try {
				results.add(new CustomWord(word, (NaturalLanguage) lang));
			} catch (InvalidLanguageCharactersException | IllegalArgumentException ignored) {}
		}

		return results;
	}


	@NonNull
	@Override
	public String toString() {
		return language.getId() + ",'" + word + "'," + sequence;
	}
}
