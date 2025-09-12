package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;

public class CustomWord {
	public final NaturalLanguage language;
	public final String word;
	public final String sequence;

	public CustomWord(@NonNull String word, NaturalLanguage language) throws InvalidLanguageCharactersException, IllegalArgumentException {
		if (word.isEmpty() || language == null) {
			throw new IllegalArgumentException("Word and language must be provided.");
		}

		this.word = word;
		this.language = language;
		this.sequence = language.getDigitSequenceForWord(word);

		if (sequence.contains("1") || sequence.contains("0")) {
			throw new IllegalArgumentException("Custom word: '" + word + "' contains punctuation.");
		}
	}

	@NonNull
	public static ArrayList<CustomWord> guessLanguage(@NonNull String word, @NonNull ArrayList<Integer> languageIds) {
		ArrayList<Language> languages = LanguageCollection.getAll(languageIds);

		ArrayList<CustomWord> results = new ArrayList<>();

		for (Language lang : languages) {
			try {
				results.add(new CustomWord(word, (NaturalLanguage) lang));
			} catch (InvalidLanguageCharactersException | IllegalArgumentException ignored) {
			}
		}

		return results;
	}
}
