package io.github.sspanak.tt9.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.extensions.ChineseExtension;
import io.github.sspanak.tt9.extensions.ChineseExtensions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.util.TextTools;

public class CustomWord {
	@NonNull public final NaturalLanguage language;
	@NonNull public final String word;
	@NonNull public final String sequence;
	@NonNull private final String explicitReading;

	public CustomWord(@NonNull String word, @NonNull String sequence, int langId) throws IllegalArgumentException {
		this(word, sequence, langId, null);
	}

	public CustomWord(@NonNull String word, @NonNull String sequence, int langId, @Nullable String reading) throws IllegalArgumentException {
		NaturalLanguage lang = LanguageCollection.getLanguage(langId);
		if (lang == null || word.isEmpty() || sequence.isEmpty()) {
			throw new IllegalArgumentException("Cannot create CustomWord out of language: " + lang + ", word: '" + word + "', sequence: '" + sequence + "'");
		}

		if (TextTools.containsPunctuation(word)) {
			throw new IllegalArgumentException("Custom word: '" + word + "' contains punctuation.");
		}

		this.language = lang;
		this.sequence = sequence;
		this.word = word;
		this.explicitReading = reading == null ? "" : reading;
	}

	public CustomWord(@NonNull String word, @Nullable NaturalLanguage language) throws InvalidLanguageCharactersException, IllegalArgumentException {
		if (word.isEmpty() || language == null) {
			throw new IllegalArgumentException("Word and language must be provided.");
		}

		this.word = word;
		this.language = language;
		this.sequence = language.getDigitSequenceForWord(word);
		this.explicitReading = "";

		if (TextTools.containsPunctuation(word)) {
			throw new IllegalArgumentException("Custom word: '" + word + "' contains punctuation.");
		}
	}

	public static CustomWord chinese(String word, String reading) {
		ChineseExtension extension = ChineseExtensions.get();
		if (extension == null) throw new IllegalArgumentException("Install the Chinese feature first");
		return extension.parseUserWord(word, reading);
	}

	public String getDisplayWord() {
		return LanguageKind.isChinesePinyin(language) ? word.substring(TextTools.lastIndexOfLatin(word) + 1) : word;
	}

	public String getReading() {
		return !explicitReading.isEmpty() ? explicitReading
			: LanguageKind.isChinesePinyin(language) ? word.substring(0, TextTools.lastIndexOfLatin(word) + 1) : "";
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
