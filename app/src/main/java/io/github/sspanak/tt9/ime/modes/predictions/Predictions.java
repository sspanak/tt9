package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract public class Predictions {
	protected final SettingsStore settings;

	// settings
	@NonNull protected String digitSequence = "";
	@NonNull protected Language language = new NullLanguage();
	protected boolean orderWordsByLength = true;
	protected int minWords = SettingsStore.SUGGESTIONS_MIN;
	protected final int maxWords = SettingsStore.SUGGESTIONS_MAX;
	protected boolean onlyExactMatches = false;
	@NonNull protected String stem = "";

	// async operations
	protected Runnable onWordsChanged = () -> {};

	// data
	protected boolean areThereDbWords = true;
	protected boolean containsGeneratedWords = false;
	@NonNull protected ArrayList<String> words = new ArrayList<>();


	public Predictions(SettingsStore settings) {
		this.settings = settings;
	}


	public void reset() {
		areThereDbWords = false;
		containsGeneratedWords = false;
		words.clear();
	}


	public Predictions setDigitSequence(@NonNull String digitSequence) {
		this.digitSequence = digitSequence;
		return this;
	}


	public Predictions setLanguage(@NonNull Language language) {
		this.language = language;
		return this;
	}


	public Predictions setMinWords(int minWords) {
		this.minWords = minWords;
		return this;
	}


	public Predictions setOnlyExactMatches(boolean onlyExactMatches) {
		this.onlyExactMatches = onlyExactMatches;
		return this;
	}


	public void setWordsChangedHandler(Runnable handler) {
		onWordsChanged = handler;
	}


	public boolean containsGeneratedWords() {
		return containsGeneratedWords;
	}


	public ArrayList<String> getList() {
		return words;
	}


	public boolean noDbWords() {
		return !areThereDbWords;
	}


	/**
	 * suggestMissingWords
	 * Takes a list of words and appends them to the words list, if they are missing.
	 */
	protected void suggestMissingWords(ArrayList<String> newWords) {
		for (String newWord : newWords) {
			if (!words.contains(newWord) && !words.contains(newWord.toLowerCase(language.getLocale()))) {
				words.add(newWord);
			}
		}
	}



	/**
	 * load
	 * Queries the dictionary database for a list of words matching the current language and sequence.
	 */
	public void load() {
		containsGeneratedWords = false;

		if (digitSequence.isEmpty()) {
			reset();
			onWordsChanged.run();
			return;
		}

		DataStore.getWords(
			(dbWords) -> onDbWords(dbWords, isRetryAllowed()),
			language,
			digitSequence,
			onlyExactMatches,
			stem,
			orderWordsByLength,
			minWords,
			maxWords
		);
	}


	abstract public void onAccept(String word, String sequence);
	abstract protected boolean isRetryAllowed();
	abstract protected void onDbWords(ArrayList<String> dbWords, boolean retryAllowed);
}
