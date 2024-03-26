package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.ime.helpers.AppHacks;
import io.github.sspanak.tt9.ime.helpers.InputModeValidator;
import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.ModePredictive;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;

public abstract class TypingHandler extends KeyPadHandler {
	// internal settings/data
	@NonNull protected AppHacks appHacks = new AppHacks(null,null, null, null);
	protected InputConnection currentInputConnection = null;
	@NonNull protected InputType inputType = new InputType(null, null);
	@NonNull protected TextField textField = new TextField(null, null);
	protected SuggestionOps suggestionOps;

	// input
	protected ArrayList<Integer> allowedInputModes = new ArrayList<>();
	@NonNull
	protected InputMode mInputMode = InputMode.getInstance(null, null, null, InputMode.MODE_PASSTHROUGH);

	// language
	protected ArrayList<Integer> mEnabledLanguages;
	protected Language mLanguage;

	@Override
	protected void createSuggestionBar(View mainView) {
		suggestionOps = new SuggestionOps(this, mainView, this::onAcceptSuggestionsDelayed);
	}

	@Override
	protected void onStart(InputConnection connection, EditorInfo field) {
		setInputField(connection, field);

		// in case we are back from Settings screen, update the language list
		mEnabledLanguages = settings.getEnabledLanguageIds();
		mLanguage = LanguageCollection.getLanguage(getApplicationContext(), settings.getInputLanguage());
		validateLanguages();

		resetKeyRepeat();
		mInputMode = getInputMode();
		determineTextCase();

		suggestionOps.setTextField(textField);
		suggestionOps.set(null);

		appHacks = new AppHacks(settings, connection, field, textField);
	}


	protected void setInputField(InputConnection connection, EditorInfo field) {
		currentInputConnection = connection;
		inputType = new InputType(currentInputConnection, field);
		textField = new TextField(currentInputConnection, field);
	}


	protected void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(getApplicationContext(), mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(getApplicationContext(), mLanguage, mEnabledLanguages);

		settings.saveEnabledLanguageIds(mEnabledLanguages);
		settings.saveInputLanguage(mLanguage.getId());
	}


	protected void onFinishTyping() {
		suggestionOps.cancelDelayedAccept();
		mInputMode = InputMode.getInstance(null, null, null, InputMode.MODE_PASSTHROUGH);
	}


	public boolean onBackspace() {
		// 1. Dialer fields seem to handle backspace on their own and we must ignore it,
		// otherwise, keyDown race condition occur for all keys.
		// 2. Allow the assigned key to function normally, when there is no text (e.g. "Back" navigates back)
		// 3. Some app may need special treatment, so let it be.
		if (mInputMode.isPassthrough() || !(textField.isThereText() || appHacks.onBackspace(mInputMode))) {
			Logger.d("onBackspace", "backspace ignored");
			mInputMode.reset();
			return false;
		}

		suggestionOps.cancelDelayedAccept();
		resetKeyRepeat();

		if (mInputMode.onBackspace()) {
			getSuggestions();
		} else {
			suggestionOps.commitCurrent(false);
			super.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
		}

		Logger.d("onBackspace", "backspace handled");
		return true;
	}


	/**
	 * onNumber
	 *
	 * @param key     Must be a number from 1 to 9, not a "KeyEvent.KEYCODE_X"
	 * @param hold    If "true" we are calling the handler, because the key is being held.
	 * @param repeat  If "true" we are calling the handler, because the key was pressed more than once
	 * @return boolean
	 */
	protected boolean onNumber(int key, boolean hold, int repeat) {
		suggestionOps.cancelDelayedAccept();
		forceShowWindowIfHidden();

		// Automatically accept the previous word, when the next one is a space or punctuation,
		// instead of requiring "OK" before that.
		// First pass, analyze the incoming key press and decide whether it could be the start of
		// a new word.
		if (mInputMode.shouldAcceptPreviousSuggestion(key)) {
			String lastWord = suggestionOps.acceptIncomplete();
			mInputMode.onAcceptSuggestion(lastWord);
			autoCorrectSpace(lastWord, false, key);
		}

		// Auto-adjust the text case before each word, if the InputMode supports it.
		if (suggestionOps.getCurrent().isEmpty()) {
			mInputMode.determineNextWordTextCase(textField.getStringBeforeCursor());
		}

		if (!mInputMode.onNumber(key, hold, repeat)) {
			return false;
		}

		if (mInputMode.shouldSelectNextSuggestion() && !suggestionOps.isEmpty()) {
			onHotkey(settings.getKeyNextSuggestion(), false, false);
			suggestionOps.scheduleDelayedAccept(mInputMode.getAutoAcceptTimeout());
		} else {
			getSuggestions();
		}

		return true;
	}


	public boolean onText(String text, boolean validateOnly) {
		if (mInputMode.shouldIgnoreText(text)) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		suggestionOps.cancelDelayedAccept();
		forceShowWindowIfHidden();

		// accept the previously typed word (if any)
		String lastWord = suggestionOps.acceptIncomplete();
		mInputMode.onAcceptSuggestion(lastWord);
		autoCorrectSpace(lastWord, false, -1);

		// "type" and accept the new word
		mInputMode.onAcceptSuggestion(text);
		textField.setText(text);
		autoCorrectSpace(text, true, -1);

		return true;
	}


	private void autoCorrectSpace(String currentWord, boolean isWordAcceptedManually, int nextKey) {
		if (mInputMode.shouldDeletePrecedingSpace(inputType)) {
			textField.deletePrecedingSpace(currentWord);
		}

		if (mInputMode.shouldAddAutoSpace(inputType, textField, isWordAcceptedManually, nextKey)) {
			textField.setText(" ");
		}
	}


	/**
	 * determineTextCase
	 * Restore the last text case or auto-select a new one. If the InputMode supports it, it can change
	 * the text case based on grammar rules, otherwise we fallback to the input field properties or the
	 * last saved mode.
	 */
	private void determineTextCase() {
		mInputMode.defaultTextCase();
		mInputMode.setTextFieldCase(textField.determineTextCase(inputType));
		mInputMode.determineNextWordTextCase(textField.getStringBeforeCursor());
		InputModeValidator.validateTextCase(mInputMode, settings.getTextCase());
	}


	/**
	 * getInputMode
	 * Load the last input mode or choose a more appropriate one.
	 * Some input fields support only numbers or are not suited for predictions (e.g. password fields)
	 */
	protected InputMode getInputMode() {
		if (!inputType.isValid() || (inputType.isLimited() && !appHacks.isTermux())) {
			return InputMode.getInstance(settings, mLanguage, inputType, InputMode.MODE_PASSTHROUGH);
		}

		allowedInputModes = textField.determineInputModes(inputType);
		int validModeId = InputModeValidator.validateMode(settings.getInputMode(), allowedInputModes);
		return InputMode.getInstance(settings, mLanguage, inputType, validModeId);
	}


	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
		// Logger.d("onUpdateSelection", "oldSelStart: " + oldSelStart + " oldSelEnd: " + oldSelEnd + " newSelStart: " + newSelStart + " oldSelEnd: " + oldSelEnd + " candidatesStart: " + candidatesStart + " candidatesEnd: " + candidatesEnd);

		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

		// If the cursor moves while composing a word (usually, because the user has touched the screen outside the word), we must
		// end typing end accept the word. Otherwise, the cursor would jump back at the end of the word, after the next key press.
		// This is confusing from user perspective, so we want to avoid it.
		if (
			candidatesStart != -1 && candidatesEnd != -1
			&& (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)
			&& !suggestionOps.isEmpty()
		) {
			mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		}
	}


	protected void onAcceptSuggestionAutomatically(String word) {
		mInputMode.onAcceptSuggestion(word, true);
		autoCorrectSpace(word, false, -1);
		mInputMode.determineNextWordTextCase(textField.getStringBeforeCursor());
	}

	private void onAcceptSuggestionsDelayed(String word) {
		onAcceptSuggestionManually(word, -1);
	}

	protected void onAcceptSuggestionManually(String word, int fromKey) {
		mInputMode.onAcceptSuggestion(word);
		if (!word.isEmpty()) {
			autoCorrectSpace(word, true, fromKey);
			resetKeyRepeat();
		}
	}



	protected void getSuggestions() {
		if (mInputMode instanceof ModePredictive && DictionaryLoader.getInstance(this).isRunning()) {
			mInputMode.reset();
			UI.toast(this, R.string.dictionary_loading_please_wait);
		} else {
			mInputMode.loadSuggestions(this::handleSuggestions, suggestionOps.getCurrent());
		}
	}


	protected void handleSuggestions() {
		// Second pass, analyze the available suggestions and decide if combining them with the
		// last key press makes up a compound word like: (it)'s, (I)'ve, l'(oiseau), or it is
		// just the end of a sentence, like: "word." or "another?"
		if (mInputMode.shouldAcceptPreviousSuggestion()) {
			String lastWord = suggestionOps.acceptPrevious(mInputMode.getSequenceLength());
			onAcceptSuggestionAutomatically(lastWord);
		}

		// display the word suggestions
		suggestionOps.set(mInputMode.getSuggestions());

		// In case we are here, because the language was changed, and there were words for the old language,
		// but there are no words for the new language, we'll get only generated suggestions, consisting
		// of the last word of the previous language + endings from the new language. These words are invalid,
		// so we discard them.
		if (mInputMode instanceof ModePredictive && !mLanguage.isValidWord(suggestionOps.getCurrent()) && !Text.isGraphic(suggestionOps.getCurrent())) {
			mInputMode.reset();
			suggestionOps.set(null);
		}

		// flush the first suggestion, if the InputMode has requested it
		if (suggestionOps.scheduleDelayedAccept(mInputMode.getAutoAcceptTimeout())) {
			return;
		}

		// Otherwise, put the first suggestion in the text field,
		// but cut it off to the length of the sequence (how many keys were pressed),
		// for a more intuitive experience.
		String trimmedWord = suggestionOps.getCurrent(mInputMode.getSequenceLength());
		appHacks.setComposingTextWithHighlightedStem(trimmedWord, mInputMode);
	}
}
