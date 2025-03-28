package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;


class LocaleWordsSorter {
	private final Language language;
	private final Pattern sortingPattern;


	LocaleWordsSorter(@Nullable Language language) {
		this.language = language;
		boolean isAlphabetWithModifiers = LanguageKind.isIndic(language) || LanguageKind.isVietnamese(language);
		sortingPattern = isAlphabetWithModifiers ? Pattern.compile("\\p{L}\\p{M}+") : null;
	}


	/**
	 * Reduces the length of a word using the sortingRegex. Usually meant to consider a
	 * base letter + modifiers as a single character.
	 */
	private int reduceLength(String word) {
		Matcher matcher = sortingPattern.matcher(word);

		int length = word.length();
		while (matcher.find()) {
			length -= matcher.end() - matcher.start() - 1;
		}

		return length;
	}


	ArrayList<String> sort(ArrayList<String> words) {
		if (sortingPattern == null || words == null || words.isEmpty()) {
			return words;
		}

		ArrayList<String> wordsCopy = new ArrayList<>(words);
		Collections.sort(wordsCopy, (a, b) -> reduceLength(a) - reduceLength(b));

		return wordsCopy;
	}


	boolean shouldSort(@NonNull String stem, @NonNull String digitSequence) {
		return
			(LanguageKind.isIndic(language) && !stem.isEmpty() && stem.length() == digitSequence.length() - 1)
			|| LanguageKind.isVietnamese(language);
	}
}
