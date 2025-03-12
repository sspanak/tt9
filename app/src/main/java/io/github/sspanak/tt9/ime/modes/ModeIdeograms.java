package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.predictions.IdeogramPredictions;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class ModeIdeograms extends ModeWords {
	protected ModeIdeograms(SettingsStore settings, Language lang, InputType inputType, TextField textField) {
		super(settings, lang, inputType, textField);
	}

	@Override
	protected void initPredictions() {
		predictions = new IdeogramPredictions(settings, textField);
		predictions.setWordsChangedHandler(this::onPredictions);
		// @todo: predictions.setMaxWords( <SOME_LARGE_NUMBER> )
		// @todo: when digitSequence.length == 1, get all words, not only the first one
		// @todo: Switching the language while typing may produce weird results on Android < 7
		// @todo: add Chinese punctuation marks, numerals and currency sign
		// @todo: documentation the new YAML properties
	}


	@Override
	public boolean onBackspace() {
		// @todo: make sure not to break characters in the middle
		return super.onBackspace();
	}

	@Override
	protected void onPredictions() {
		// @todo: stripping transcriptions should be optional, based on "filterBySound"
		((IdeogramPredictions) predictions).stripTranscriptions();
		// @todo: when filtering is on, keep only the latin letters instead

		super.onPredictions();
	}

	@Override public void determineNextWordTextCase() {}
	@Override protected String adjustSuggestionTextCase(String word, int newTextCase) { return word; }

	// @todo: maybe implement this?
	@Override public boolean recompose(String word) { return false; }

	@Override
	public boolean shouldAcceptPreviousSuggestion(String unacceptedText) {
		// @todo: implement...
		return false;
	}

	@Override
	public boolean shouldAcceptPreviousSuggestion(int nextKey, boolean hold) {
		// @todo: implement...
		return hold;
	}

	@Override
	public void onAcceptSuggestion(@NonNull String word) {
		// @todo: implement rules for filtering. Must switch from Latin to Logograms
		// @todo: implement rules for Chinese here or in another class?
		super.onAcceptSuggestion(word);
	}

	@Override
	public void onAcceptSuggestion(@NonNull String currentWord, boolean preserveWords) {
		// @todo: implement rules for filtering. Must switch from Latin to Logograms
		// @todo: implement rules for Chinese here or in another class?
		super.onAcceptSuggestion(currentWord, preserveWords);
	}
}
