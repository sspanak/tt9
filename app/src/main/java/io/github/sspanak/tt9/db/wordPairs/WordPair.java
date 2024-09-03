package io.github.sspanak.tt9.db.wordPairs;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Language;

public class WordPair {
	private final String word1;
	private final String word2;

	public WordPair(Language language, String word1, String word2) {
		this.word1 = word1.toLowerCase(language.getLocale());
		this.word2 = word2.toLowerCase(language.getLocale());
	}

	String getWord1() {
		return word1;
	}

	String getWord2() {
		return word2;
	}

	boolean equals(Language language, String word1, String word2) {
		return this.word1.equals(word1.toLowerCase(language.getLocale())) && this.word2.equals(word2.toLowerCase(language.getLocale()));
	}

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.put("word1", word1);
		values.put("word2", word2);
		return values;
	}

	@NonNull
	@Override
	public String toString() {
		return "(" + word1 + "," + word2 + ")";
	}
}
