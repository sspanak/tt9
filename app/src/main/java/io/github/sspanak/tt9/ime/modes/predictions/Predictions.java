package io.github.sspanak.tt9.ime.modes.predictions;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.NullLanguage;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract public class Predictions {
	protected final SettingsStore settings;

	@NonNull protected String digitSequence;
	@NonNull protected Language language;
	@NonNull protected String stem;

	// async operations
	protected Runnable onWordsChanged = () -> {};

	// data
	protected boolean areThereDbWords = false;
	protected boolean containsGeneratedWords = false;
	@NonNull protected ArrayList<String> words = new ArrayList<>();

	public Predictions(SettingsStore settings) {
		digitSequence = "";
		language = new NullLanguage();
		this.settings = settings;
		stem = "";
	}

	public Predictions setLanguage(Language language) {
		this.language = language;
		return this;
	}

	public Predictions setDigitSequence(String digitSequence) {
		this.digitSequence = digitSequence;
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
	 * load
	 * Queries the dictionary database for a list of words matching the current language and sequence.
	 */
	public void load() {
		containsGeneratedWords = false;

		if (digitSequence.isEmpty()) {
			words.clear();
			onWordsChanged.run();
			return;
		}

		DataStore.getWords(
			(dbWords) -> onDbWords(dbWords, isRetryAllowed()),
			language,
			digitSequence,
			stem,
			SettingsStore.SUGGESTIONS_MIN,
			SettingsStore.SUGGESTIONS_MAX
		);
	}

	abstract public void onAccept(String word, String sequence);
	abstract protected boolean isRetryAllowed();
	abstract protected void onDbWords(ArrayList<String> dbWords, boolean retryAllowed);
}
