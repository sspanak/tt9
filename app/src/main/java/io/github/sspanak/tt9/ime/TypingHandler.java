package io.github.sspanak.tt9.ime;

import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Text;

public abstract class TypingHandler extends KeyPadHandler {
	// internal settings/data
	@NonNull protected AppHacks appHacks = new AppHacks(null, null, null);
	@NonNull protected InputType inputType = new InputType(null, null);
	@NonNull protected TextField textField = new TextField(null, null, null);
	@NonNull protected TextSelection textSelection = new TextSelection(null);
	@NonNull protected SuggestionOps suggestionOps = new SuggestionOps(null, null, null, null, null, null);
	@NonNull private Handler shiftStateDebounceHandler = new Handler(Looper.getMainLooper());

	// input
	@NonNull protected ArrayList<Integer> allowedInputModes = new ArrayList<>();
	@NonNull protected InputMode mInputMode = InputMode.getInstance(null, null, null, null, InputMode.MODE_PASSTHROUGH);

	// language
	protected ArrayList<Integer> mEnabledLanguages;
	protected Language mLanguage;


	protected void createSuggestionBar() {
		suggestionOps = new SuggestionOps(this, settings, mainView, textField, this::onAcceptSuggestionsDelayed, this::onOK);
	}


	protected boolean shouldBeOff() {
		return getCurrentInputConnection() == null || InputModeKind.isPassthrough(mInputMode);
	}

	@Override
	protected boolean onStart(EditorInfo field) {
		boolean restart = textField.equals(getCurrentInputConnection(), field);

		setInputField(field);

		// 1. In case we are back from Settings screen, update the language list
		// 2. If the connected app hints it is in a language different than the current one,
		// we try to switch.
		boolean languageChanged = determineLanguage();

		// ignore multiple calls for the same field, caused by requestShowSelf() -> showWindow(),
		// or weirdly functioning apps, such as the Qin SMS app
		if (restart && !languageChanged && mInputMode.getId() == determineInputModeId()) {
			return false;
		}

		settings.setDefaultCharOrder(mLanguage, false);
		resetKeyRepeat();
		mInputMode = determineInputMode();
		determineTextCase();
		updateShiftState(true, false);
		suggestionOps.set(null);

		return true;
	}


	protected void setInputField(EditorInfo field) {
		if (textField.equals(getCurrentInputConnection(), field)) {
			return;
		}

		InputMethodService context = field != null ? this : null;
		inputType = new InputType(context, field);
		textField = new TextField(context, settings, field);
		textSelection = new TextSelection(context);

		// changing the TextField and notifying all interested classes is an atomic operation
		appHacks = new AppHacks(inputType, textField, textSelection);
		suggestionOps.setTextField(textField);
	}


	protected void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(mLanguage, mEnabledLanguages);
		settings.saveInputLanguage(mLanguage.getId());
		settings.saveEnabledLanguageIds(mEnabledLanguages);
	}


	protected void onFinishTyping() {
		suggestionOps.cancelDelayedAccept();
		mInputMode = InputMode.getInstance(null, null, null, null, InputMode.MODE_PASSTHROUGH);
		setInputField(null);
	}


	@Override
	public boolean onBackspace(int repeat) {
		// Dialer fields seem to handle backspace on their own and we must ignore it,
		// otherwise, keyDown race condition occur for all keys.
		if (InputModeKind.isPassthrough(mInputMode)) {
			return false;
		}

		if (appHacks.onBackspace(settings, mInputMode)) {
			mInputMode.reset();
			mainView.renderDynamicKeys();
			return false;
		}

		suggestionOps.cancelDelayedAccept();
		resetKeyRepeat();

		if (settings.getBackspaceAcceleration() && repeat > 0 && repeat % SettingsStore.BACKSPACE_ACCELERATION_REPEAT_DEBOUNCE != 0) {
			return true;
		}

		mInputMode.beforeDeleteText();

		boolean noTextSelection = textSelection.isEmpty(); // loading words after deleting selected text is confusing
		if (repeat == 0 && mInputMode.onBackspace() && noTextSelection) {
			getSuggestions(null);
		} else {
			suggestionOps.commitCurrent(false, true);
			mInputMode.reset();
			deleteText(settings.getBackspaceAcceleration() && repeat > 0);
			updateShiftStateDebounced(mInputMode.getSuggestions().isEmpty(), false);
		}

		if (settings.getBackspaceRecomposing() && repeat == 0 && noTextSelection && suggestionOps.isEmpty() && !DictionaryLoader.getInstance(this).isRunning()) {
			final String previousWord = mInputMode.recompose();
			if (textField.recompose(previousWord)) {
				getSuggestions(previousWord);
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

		// In Korean, the next char may "steal" components from the previous one, in which case,
		// we must replace the previous char with a one containing less strokes.
		if (mInputMode.shouldReplaceLastLetter(key, hold)) {
			mInputMode.replaceLastLetter();
		}
		// Automatically accept the previous word, when the next one is a space or punctuation,
		// instead of requiring "OK" before that.
		// First pass, analyze the incoming key press and decide whether it could be the start of
		// a new word. In case we do accept it, we preserve the suggestion list instead of clearing,
		// to prevent flashing while the next suggestions are being loaded.
		else if (mInputMode.shouldAcceptPreviousSuggestion(suggestionOps.getCurrent(), key, hold)) {
			// WARNING! Ensure the code after "acceptIncompleteAndKeepList()" does not depend on
			// the suggestions in SuggestionOps, since we don't clear that list.
			String lastWord = suggestionOps.acceptIncompleteAndKeepList();
			mInputMode.onAcceptSuggestion(lastWord);
			autoCorrectSpace(lastWord, false, key);
		}

		// Auto-adjust the text case before each word, if the InputMode supports it.
		if (mInputMode.getSuggestions().isEmpty()) {
			mInputMode.determineNextWordTextCase(key);
		}

		if (!mInputMode.onNumber(key, hold, repeat)) {
			forceShowWindow();
			return false;
		}

		if (mInputMode.shouldSelectNextSuggestion() && !mInputMode.getSuggestions().isEmpty()) {
			scrollSuggestions(false);
			suggestionOps.scheduleDelayedAccept(mInputMode.getAutoAcceptTimeout());
		} else {
			getSuggestions(null);
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
		updateShiftState(true, false);

		return true;
	}


	private void autoCorrectSpace(String currentWord, boolean isWordAcceptedManually, int nextKey) {
		if (!inputType.isRustDesk() && mInputMode.shouldDeletePrecedingSpace()) {
			textField.deletePrecedingSpace(currentWord);
		}

		if (mInputMode.shouldAddPrecedingSpace()) {
			textField.addPrecedingSpace(currentWord);
		}

		if (mInputMode.shouldAddTrailingSpace(isWordAcceptedManually, nextKey)) {
			textField.setText(" ");
		}
	}


	private void deleteText(boolean deleteMany) {
		int charsToDelete = 1;

		if (!textSelection.isEmpty()) {
			charsToDelete = textSelection.length();
			textSelection.clear(false);
		} else if (deleteMany) {
			charsToDelete = textField.getComposingText().length();
			charsToDelete = charsToDelete > 0 ? charsToDelete : Math.max(textField.getPaddedWordBeforeCursorLength(), 1);
		}

		textField.deleteChars(charsToDelete);
	}


	/**
	 * determineLanguage
	 * Restore the last language or auto-select a more appropriate one, if the application hints so.
	 * In case the settings are not valid, we will fallback to the default language.
	 */
	private boolean determineLanguage() {
		mEnabledLanguages = settings.getEnabledLanguageIds();

		int oldLang = mLanguage != null ? mLanguage.getId() : -1;
		mLanguage = LanguageCollection.getLanguage(settings.getInputLanguage());
		validateLanguages();

		Language appLanguage = textField.getLanguage(mEnabledLanguages);
		if (appLanguage != null) {
			mLanguage = appLanguage;
		}

		return oldLang != mLanguage.getId();
	}


	/**
	 * determineTextCase
	 * Restore the last used text case or auto-select a new one based on the input field properties.
	 */
	protected void determineTextCase() {
		InputModeValidator.validateTextCase(mInputMode, settings.getTextCase());
	}


	/**
	 * determineInputModeId
	 * Return the last input mode ID or choose a more appropriate one.
	 * Some input fields support only numbers or are not suited for predictions (e.g. password fields).
	 * Others do not support text retrieval or composing text, or the AppHacks detected them as incompatible with us.
	 * We do not want to handle any of these, hence we pass through all input to the system.
	 */
	protected int determineInputModeId() {
		if (!inputType.isValid() || (inputType.isLimited() && !inputType.isTeams() && !inputType.isTermux())) {
			return InputMode.MODE_PASSTHROUGH;
		}

		allowedInputModes = new ArrayList<>(inputType.determineInputModes(getApplicationContext()));
		if (LanguageKind.isJapanese(mLanguage)) {
			determineJapaneseInputModes();
		} else if (!mLanguage.hasABC()) {
			allowedInputModes.remove((Integer) InputMode.MODE_ABC);
		} else if (!settings.getPredictiveMode()) {
			allowedInputModes.remove((Integer) InputMode.MODE_PREDICTIVE);
		}

		return InputModeValidator.validateMode(settings.getInputMode(), allowedInputModes);
	}


	/**
	 * Since Japanese is unique with its 3 alphabets, we need to setup the input modes separately.
	 */
	private void determineJapaneseInputModes() {
		if (allowedInputModes.contains(InputMode.MODE_ABC)) {
			allowedInputModes.remove((Integer) InputMode.MODE_ABC);
			allowedInputModes.add(InputMode.MODE_HIRAGANA);
			allowedInputModes.add(InputMode.MODE_KATAKANA);
		}
		if (!settings.getPredictiveMode()) {
			allowedInputModes.remove((Integer) InputMode.MODE_PREDICTIVE);
		}
	}


	/**
	 * determineInputMode
	 * Same as determineInputModeId(), but returns an actual InputMode.
	 */
	protected InputMode determineInputMode() {
		return InputMode.getInstance(settings, mLanguage, inputType, textField, determineInputModeId());
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
		if (CursorOps.isMovedManually(newSelStart, newSelEnd, candidatesStart, candidatesEnd)) {
			mInputMode.onCursorMove(suggestionOps.acceptIncomplete());
		}
	}


	protected void onAcceptSuggestionAutomatically(String word) {
		mInputMode.onAcceptSuggestion(word, true);
		autoCorrectSpace(word, false, mInputMode.getSequence().isEmpty() ? -1 : mInputMode.getSequence().charAt(0) - '0');
		mInputMode.determineNextWordTextCase(-1);
	}

	private void onAcceptSuggestionsDelayed(String word) {
		onAcceptSuggestionManually(word, -1);
		forceShowWindow();
	}

	protected void onAcceptSuggestionManually(String word, int fromKey) {
		mInputMode.onAcceptSuggestion(word);
		if (!word.isEmpty()) {
			autoCorrectSpace(word, true, fromKey);
			updateShiftState(true, false);
			resetKeyRepeat();
		}
	}


	@NonNull
	@Override
	public SuggestionOps getSuggestionOps() {
		return suggestionOps;
	}


	protected void getSuggestions(@Nullable String currentWord) {
		if (InputModeKind.isPredictive(mInputMode) && DictionaryLoader.getInstance(this).isRunning()) {
			mInputMode.reset();
			UI.toastShortSingle(this, R.string.dictionary_loading_please_wait);
		} else {
			mInputMode
				.setOnSuggestionsUpdated(this::handleSuggestions)
				.loadSuggestions(currentWord == null ? suggestionOps.getCurrent() : currentWord);
		}
	}


	protected void handleSuggestions() {
		// Second pass, analyze the available suggestions and decide if combining them with the
		// last key press makes up a compound word like: (it)'s, (I)'ve, l'(oiseau), or it is
		// just the end of a sentence, like: "word." or "another?"
		if (mInputMode.shouldAcceptPreviousSuggestion(suggestionOps.getCurrent())) {
			String lastWord = suggestionOps.acceptPrevious(mLanguage, mInputMode.getSequenceLength());
			onAcceptSuggestionAutomatically(lastWord);
		}

		// display the word suggestions
		suggestionOps.set(mInputMode.getSuggestions(), mInputMode.containsGeneratedSuggestions());

		// flush the first suggestion, if the InputMode has requested it
		if (suggestionOps.scheduleDelayedAccept(mInputMode.getAutoAcceptTimeout())) {
			return;
		}

		// Otherwise, put the first suggestion in the text field,
		// but cut it off to the length of the sequence (how many keys were pressed),
		// for a more intuitive experience.
		String trimmedWord = suggestionOps.getCurrent(mLanguage, mInputMode.getSequenceLength());
		appHacks.setComposingTextWithHighlightedStem(trimmedWord, mInputMode);

		if (mInputMode.getSuggestions().isEmpty()) {
			updateShiftStateDebounced(true, false);
		} else {
			updateShiftStateDebounced(false, true);
		}

		forceShowWindow();
	}


	protected void scrollSuggestions(boolean backward) {
		suggestionOps.cancelDelayedAccept();
		suggestionOps.scrollTo(backward ? -1 : 1);
		mInputMode.setWordStem(suggestionOps.getCurrent(), true);
		appHacks.setComposingTextWithHighlightedStem(suggestionOps.getCurrent(), mInputMode);
	}


	protected void updateShiftStateDebounced(boolean determineTextCase, boolean onlyWhenLetters) {
		shiftStateDebounceHandler.removeCallbacksAndMessages(null);
		shiftStateDebounceHandler.postDelayed(() -> updateShiftState(determineTextCase, onlyWhenLetters), SettingsStore.SHIFT_STATE_DEBOUNCE_TIME);
	}


	protected void updateShiftState(boolean determineTextCase, boolean onlyWhenLetters) {
		if (onlyWhenLetters && !new Text(suggestionOps.getCurrent()).isAlphabetic()) {
			return;
		}

		if (determineTextCase) {
			mInputMode.determineNextWordTextCase(-1);
		}

		setStatusIcon(mInputMode, mLanguage);
		mainView.renderDynamicKeys();
		if (!mainView.isTextEditingPaletteShown() && !mainView.isCommandPaletteShown()) {
			statusBar.setText(mInputMode);
		}
	}
}
