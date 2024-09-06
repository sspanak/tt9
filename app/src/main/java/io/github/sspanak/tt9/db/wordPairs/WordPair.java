package io.github.sspanak.tt9.db.wordPairs;

import android.content.ContentValues;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;

public class WordPair {
	private final Language language;
	@NonNull private final String word1;
	@NonNull private final String word2;
	private final String sequence2;
	private Integer hash = null;


	public WordPair(Language language, String word1, String word2, String sequence2) {
		this.language = language;
		this.word1 = word1 != null ? word1.toLowerCase(language.getLocale()) : "";
		this.word2 = word2 != null ? word2.toLowerCase(language.getLocale()) : "";
		this.sequence2 = sequence2;
	}


	boolean isInvalid() {
		return
			language == null
			|| word1.isEmpty() || word2.isEmpty()
			|| (word1.length() > SettingsStore.WORD_PAIR_MAX_WORD_LENGTH && word2.length() > SettingsStore.WORD_PAIR_MAX_WORD_LENGTH)
			|| word1.equals(word2)
			|| sequence2 == null || word2.length() != sequence2.length()
			|| !(new Text(word1).isAlphabetic()) || !(new Text(word2).isAlphabetic());
	}


	@NonNull
	public String getWord2() {
		return word2;
	}


	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.put("word1", word1);
		values.put("word2", word2);
		values.put("sequence2", sequence2);
		return values;
	}


	@Override
	public int hashCode() {
		if (hash == null) {
			hash = !word1.isEmpty() && sequence2 != null ? (word1 + "," + sequence2).hashCode() : 0;
		}

		return hash;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		return obj instanceof WordPair && obj.hashCode() == hashCode();
	}


	@NonNull
	@Override
	public String toString() {
		return "(" + word1 + "," + word2 + "," + sequence2 + ")";
	}
}
