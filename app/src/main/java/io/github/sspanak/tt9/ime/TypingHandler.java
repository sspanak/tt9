package io.github.sspanak.tt9.ime;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.hacks.AppHacks;
import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.CursorOps;
import io.github.sspanak.tt9.ime.helpers.InputModeValidator;
import io.github.sspanak.tt9.ime.helpers.SuggestionOps;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.helpers.TextSelection;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.ModePredictive;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Text;

public abstract class TypingHandler extends KeyPadHandler {
	// internal settings/data
	@NonNull protected AppHacks appHacks = new AppHacks(null,null, null, null, null);
	protected InputConnection currentInputConnection = null;
	@NonNull protected InputType inputType = new InputType(null, null);
	@NonNull protected TextField textField = new TextField(null, null);
	@NonNull protected TextSelection textSelection = new TextSelection(this,null);
	protected SuggestionOps suggestionOps;

	// input
	protected ArrayList<Integer> allowedInputModes = new ArrayList<>();
	@NonNull
	protected InputMode mInputMode = InputMode.getInstance(null, null, null, null, InputMode.MODE_PASSTHROUGH);

	// language
	protected ArrayList<Integer> mEnabledLanguages;
	protected Language mLanguage;


	protected void createSuggestionBar() {
		suggestionOps = new SuggestionOps(settings, mainView, textField, this::onAcceptSuggestionsDelayed, this::onOK);
	}


	protected boolean shouldBeOff() {
		return currentInputConnection == null || mInputMode.isPassthrough();
	}

	@Override
	protected boolean onStart(InputConnection connection, EditorInfo field) {
		boolean restart = textField.equals(connection, field);

		setInputField(connection, field);

		// 1. In case we are back from Settings screen, update the language list
		// 2. If the connected app hints it is in a language different than the current one,
		// we try to switch.
		boolean languageChanged = determineLanguage();

		// ignore multiple calls for the same field, caused by requestShowSelf() -> showWindow(),
		// or weirdly functioning apps, such as the Qin SMS app
		if (restart && !languageChanged && mInputMode.getId() == getInputModeId()) {
			return false;
		}

		resetKeyRepeat();
		mInputMode = getInputMode();
		determineTextCase();
		suggestionOps.set(null);

		return true;
	}


	protected void setInputField(InputConnection connection, EditorInfo field) {
		if (textField.equals(connection, field)) {
			return;
		}

		currentInputConnection = connection;
		inputType = new InputType(currentInputConnection, field);
		textField = new TextField(currentInputConnection, field);
		textSelection = new TextSelection(this, currentInputConnection);

		// changing the TextField and notifying all interested classes is an atomic operation
		appHacks = new AppHacks(settings, connection, inputType, textField, textSelection);
		suggestionOps.setTextField(textField);
	}


	protected void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(getApplicationContext(), mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(getApplicationContext(), mLanguage, mEnabledLanguages);
		settings.saveInputLanguage(mLanguage.getId());
		settings.saveEnabledLanguageIds(mEnabledLanguages);
	}


	protected void onFinishTyping() {
		suggestionOps.cancelDelayedAccept();
		mInputMode = InputMode.getInstance(null, null, null, null, InputMode.MODE_PASSTHROUGH);
		setInputField(null, null);
	}


	@Override
	public boolean onBackspace(boolean hold) {
		// Dialer fields seem to handle backspace on their own and we must ignore it,
		// otherwise, keyDown race condition occur for all keys.
		if (mInputMode.isPassthrough()) {
			return false;
		}

		if (appHacks.onBackspace(mInputMode)) {
			mInputMode.reset();
			return false;
		}

		suggestionOps.cancelDelayedAccept();
		resetKeyRepeat();

		if (!hold && mInputMode.onBackspace()) {
			getSuggestions();
		} else {
			suggestionOps.commitCurrent(false);
			mInputMode.reset();

			int prevChars = hold ? Math.max(textField.getPaddedWordBeforeCursorLength(), 1) : 1;
			textField.deleteChars(prevChars);
		}

		if (settings.getBackspaceRecomposing() && !hold && suggestionOps.isEmpty()) {
			final String previousWord = textField.getWordBeforeCursor(mLanguage, 0, false);
			if (mInputMode.recompose(previousWord) && textField.recompose(previousWord)) {
				getSuggestions();
			} else {
				mInputMode.reset();
			}
		}

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
			forceShowWindow();
			return false;
		}

		if (mInputMode.shouldSelectNextSuggestion() && !suggestionOps.isEmpty()) {
			scrollSuggestions(false);
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

		// accept the previously typed word (if any)
		String lastWord = suggestionOps.acceptIncomplete();
		mInputMode.onAcceptSuggestion(lastWord);
		autoCorrectSpace(lastWord, false, -1);

		// "type" and accept the new word
		mInputMode.onAcceptSuggestion(text);
		textField.setText(text);
		autoCorrectSpace(text, true, -1);

		forceShowWindow();
		return true;
	}


	private void autoCorrectSpace(String currentWord, boolean isWordAcceptedManually, int nextKey) {
		if (!inputType.isRustDesk() && mInputMode.shouldDeletePrecedingSpace(inputType)) {
			textField.deletePrecedingSpace(currentWord);
		}

		if (mInputMode.shouldAddAutoSpace(inputType, textField, isWordAcceptedManually, nextKey)) {
			textField.setText(" ");
		}
	}


	/**
	 * determineLanguage
	 * Restore the last language or auto-select a more appropriate one, if the application hints so.
	 * In case the settings are not valid, we will fallback to the default language.
	 */
	private boolean determineLanguage() {
		mEnabledLanguages = settings.getEnabledLanguageIds();

		int oldLang = mLanguage != null ? mLanguage.getId() : -1;
		mLanguage = LanguageCollection.getLanguage(getApplicationContext(), settings.getInputLanguage());
		validateLanguages();

		Language appLanguage = textField.getLanguage(getApplicationContext(), mEnabledLanguages);
		if (appLanguage != null) {
			mLanguage = appLanguage;
		}

		return oldLang != mLanguage.getId();
	}


	/**
	 * determineTextCase
	 * Restore the last text case or auto-select a new one. If the InputMode supports it, it can change
	 * the text case based on grammar rules, otherwise we fallback to the input field properties or the
	 * last saved mode.
	 */
	private void determineTextCase() {
		mInputMode.defaultTextCase();
		mInputMode.setTextFieldCase(inputType.determineTextCase());
		mInputMode.determineNextWordTextCase(textField.getStringBeforeCursor());
		InputModeValidator.validateTextCase(mInputMode, settings.getTextCase());
	}


	/**
	 * getInputModeId
	 * Return the last input mode ID or choose a more appropriate one.
	 * Some input fields support only numbers or are not suited for predictions (e.g. password fields).
	 * Others do not support text retrieval or composing text, or the AppHacks detected them as incompatible with us.
	 * We do not want to handle any of these, hence we pass through all input to the system.
	 */
	protected int getInputModeId() {
		if (!inputType.isValid() || (inputType.isLimited() && !inputType.isTermux())) {
			return InputMode.MODE_PASSTHROUGH;
		}

		allowedInputModes = inputType.determineInputModes(this);
		return InputModeValidator.validateMode(settings.getInputMode(), allowedInputModes);
	}


	/**
	 * getInputMode
	 * Same as getInputModeId(), but returns an actual InputMode.
	 */
	protected InputMode getInputMode() {
		return InputMode.getInstance(settings, mLanguage, inputType, textField, getInputModeId());
	}


	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
		// Logger.d("onUpdateSelection", "oldSelStart: " + oldSelStart + " oldSelEnd: " + oldSelEnd + " newSelStart: " + newSelStart + " oldSelEnd: " + oldSelEnd + " candidatesStart: " + candidatesStart + " candidatesEnd: " + candidatesEnd);

		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
		textSelection.onSelectionUpdate(newSelStart, newSelEnd);

		// in case the app has modified the InputField and moved the cursor without notifying us...
		if (appHacks.onUpdateSelection(mInputMode, suggestionOps, oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)) {
			return;
		}

		// If the cursor moves while composing a word (usually, because the user has touched the screen outside the word), we must
		// end typing end accept the word. Otherwise, the cursor would jump back at the end of the word, after the next key press.
		// This is confusing from user perspective, so we want to avoid it.
		if (
			CursorOps.isMovedManually(newSelStart, newSelEnd, candidatesStart, candidatesEnd)
			&& !suggestionOps.isEmpty()
		) {
			mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		}
	}


	protected void onAcceptSuggestionAutomatically(String word) {
		mInputMode.onAcceptSuggestion(word, true);
		autoCorrectSpace(word, false, mInputMode.getSequence().isEmpty() ? -1 : mInputMode.getSequence().charAt(0) - '0');
		mInputMode.determineNextWordTextCase(textField.getStringBeforeCursor());
	}

	private void onAcceptSuggestionsDelayed(String word) {
		onAcceptSuggestionManually(word, -1);
		forceShowWindow();
	}

	protected void onAcceptSuggestionManually(String word, int fromKey) {
		mInputMode.onAcceptSuggestion(word);
		if (!word.isEmpty()) {
			autoCorrectSpace(word, true, fromKey);
			resetKeyRepeat();
		}
	}


	@Override
	public SuggestionOps getSuggestionOps() {
		return suggestionOps;
	}

	protected void getSuggestions() {
		if (mInputMode instanceof ModePredictive && DictionaryLoader.getInstance(this).isRunning()) {
			mInputMode.reset();
			UI.toastShortSingle(this, R.string.dictionary_loading_please_wait);
		} else {
			mInputMode
				.setOnSuggestionsUpdated(this::handleSuggestions)
				.loadSuggestions(suggestionOps.getCurrent());
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
		suggestionOps.set(mInputMode.getSuggestions(), mInputMode.containsGeneratedSuggestions());

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

		forceShowWindow();
	}


	protected void scrollSuggestions(boolean backward) {
		suggestionOps.cancelDelayedAccept();
		suggestionOps.scrollTo(backward ? -1 : 1);
		mInputMode.setWordStem(suggestionOps.getCurrent(), true);
		appHacks.setComposingTextWithHighlightedStem(suggestionOps.getCurrent(), mInputMode);
	}
}
