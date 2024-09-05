package io.github.sspanak.tt9.db.wordPairs;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Set;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;

public class WordPair {
	private final Language language;
	private final String word1;
	private final String word2;

	public static ArrayList<WordPair> fromPairStringSet(Language language, Set<String> pairStrings) {
		ArrayList<WordPair> wordPairs = new ArrayList<>();
		for (String pairString : pairStrings) {
			wordPairs.add(new WordPair(language, pairString));
		}
		return wordPairs;
	}

	WordPair(Language language, String pairString) {
		String[] words = pairString.split(",");
		if (words.length == 2) {
			this.language = language;
			word1 = words[0].replace("(", "").replace(")", "").trim().toLowerCase(language.getLocale());
			word2 = words[1].replace("(", "").replace(")", "").trim().toLowerCase(language.getLocale());
		} else {
			this.language = null;
			word1 = null;
			word2 = null;
		}
	}

	public WordPair(Language language, String word1, String word2) {
		this.language = language;
		this.word1 = word1.toLowerCase(language.getLocale());
		this.word2 = word2.toLowerCase(language.getLocale());
	}

	boolean isInvalid() {
		return
			language == null
			|| word1 == null || word2 == null
			|| word1.isEmpty() || word2.isEmpty()
			|| (word1.length() > SettingsStore.WORD_PAIR_MAX_WORD_LENGTH && word2.length() > SettingsStore.WORD_PAIR_MAX_WORD_LENGTH)
			|| word1.equals(word2)
			|| !(new Text(word1).isAlphabetic()) || !(new Text(word2).isAlphabetic());
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
