package io.github.sspanak.tt9.ime;

import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.helpers.CursorOps;
import io.github.sspanak.tt9.ime.helpers.InputConnectionAsync;
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
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;

public abstract class TypingHandler extends KeyPadHandler {
	// internal settings/data
	@NonNull protected InputType inputType = new InputType(null, null);
	@NonNull protected TextField textField = new TextField(null, null, null);
	@NonNull protected TextSelection textSelection = new TextSelection(null, null);
	@NonNull protected SuggestionOps suggestionOps = new SuggestionOps(null, null, null, null, null, null, null, null, null);

	@Nullable private Handler shiftStateDebounceHandler;

	// input
	@NonNull protected ArrayList<Integer> allowedInputModes = new ArrayList<>();
	@NonNull protected InputMode mInputMode = InputMode.getInstance(null, null, null, null, InputMode.MODE_PASSTHROUGH);

	// language
	protected ArrayList<Integer> mEnabledLanguages;
	protected Language mLanguage;


	abstract protected void onAcceptSuggestionsDelayed(String s);
	abstract protected void getSuggestions(@Nullable String currentWord, @Nullable Runnable onComplete);
	abstract protected void guessNextWord(@NonNull String[] surroundingText, @Nullable String currentWord, boolean saveContext);
	abstract protected boolean clearGuessingContext();
	abstract protected void setGuessingContext(@NonNull String[] surroundingText, @Nullable String currentWord);


	protected void createSuggestionBar() {
		suggestionOps = new SuggestionOps(this, settings, mainView, appHacks, inputType, textField, statusBar, this::onAcceptSuggestionsDelayed, this::onOK);
	}


	protected boolean shouldBeOff() {
		return getCurrentInputConnection() == null || InputModeKind.isPassthrough(mInputMode);
	}


	@Override
	protected boolean onStart(EditorInfo field, boolean restarting) {
		boolean restart = restarting || textField.equals(getCurrentInputConnection(), field);

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
		suggestionOps.set(null);

		// don't use surroundingText cache on start up
		final String[] surroundingText = textField.getSurroundingStringForAutoAssistance(settings, mInputMode);
		updateShiftState(surroundingText[0], true, false);
		setGuessingContext(surroundingText, null);

		return true;
	}


	@Override
	public void onDestroy() {
		InputConnectionAsync.destroy();
		super.onDestroy();
	}


	protected void setInputField(EditorInfo field) {
		if (textField.equals(getCurrentInputConnection(), field)) {
			return;
		}

		InputMethodService context = field != null ? this : null;
		inputType = new InputType(context, field);
		textField = new TextField(context, settings, field);
		textSelection = new TextSelection(context, inputType);

		// changing the TextField and notifying all interested classes is an atomic operation
		appHacks.setDependencies(inputType, textField, textSelection);
		suggestionOps.setDependencies(appHacks, inputType, textField, statusBar);
	}


	protected void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(mLanguage, mEnabledLanguages);
		settings.saveInputLanguage(mLanguage.getId());
		settings.saveEnabledLanguageIds(mEnabledLanguages);
	}


	protected void onFinishTyping() {
		if (shiftStateDebounceHandler != null) {
			shiftStateDebounceHandler.removeCallbacksAndMessages(null);
			shiftStateDebounceHandler = null;
		}
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

		if (clearGuessingContext()) {
			suggestionOps.clear(); // @todo: this may not be necessary?
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

		// load new words only if there is no selected text, because it would be confusing
		if (repeat == 0 && mInputMode.onBackspace() && textSelection.isEmpty()) {
			final Runnable onLoad = InputModeKind.isRecomposing(mInputMode) ? null : () -> recompose(repeat, false);
			getSuggestions(null, onLoad);
		} else {
			suggestionOps.commitCurrent(false, true);
			mInputMode.reset();
			deleteText(settings.getBackspaceAcceleration() && repeat > 0);
			updateShiftStateDebounced(null, mInputMode.noSuggestions(), false); // backspace may change the text too much, so no beforeCursor cache for now
			recompose(repeat, !textSelection.isEmpty());
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

		hold = hold && settings.getHoldToType();
		String[] surroundingChars = textField.getSurroundingStringForAutoAssistance(settings, mInputMode);

		// Automatically accept the previous word, when the next one is a space or punctuation,
		// instead of requiring "OK" before that.
		// First pass, analyze the incoming key press and decide whether it could be the start of
		// a new word. In case we do accept it, we preserve the suggestion list instead of clearing,
		// to prevent flashing while the next suggestions are being loaded.
		if (mInputMode.shouldAcceptPreviousSuggestion(suggestionOps.getCurrent(), key, hold)) {
			// WARNING! Ensure the code after "acceptIncompleteAndKeepList()" does not depend on
			// the suggestions in SuggestionOps, since we don't clear that list.
			String lastWord = suggestionOps.acceptIncompleteAndKeepList();
			mInputMode.onAcceptSuggestion(lastWord);
			surroundingChars = autoCorrectSpace(lastWord, surroundingChars, false, key);

			if (mLanguage.hasSpaceBetweenWords()) {
				setGuessingContext(surroundingChars, lastWord);
			} else {
				guessNextWord(surroundingChars, lastWord, true);
			}
		}

		// Auto-adjust the text case before each word/char, if the InputMode supports it.
		mInputMode.determineNextWordTextCase(surroundingChars[0], key);

		if (!mInputMode.onNumber(key, hold, repeat, surroundingChars)) {
			forceShowWindow();
			return false;
		}

		if (mInputMode.shouldSelectNextSuggestion() && !mInputMode.noSuggestions()) {
			scrollSuggestions(false);
			suggestionOps.scheduleDelayedAccept(mInputMode.getAutoAcceptTimeout());
		} else {
			getSuggestions(null, null);
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

		String[] surroundingChars;

		// accept the previously typed word (if any)
		String lastWord = suggestionOps.acceptIncomplete();
		if (lastWord.isEmpty()) {
			surroundingChars = textField.getSurroundingStringForAutoAssistance(settings, mInputMode);
		} else {
			mInputMode.onAcceptSuggestion(lastWord);
			surroundingChars = autoCorrectSpace(
				lastWord,
				textField.getSurroundingStringForAutoAssistance(settings, mInputMode),
				false,
				-1
			);
		}

		// "type" and accept the new word
		mInputMode.onAcceptSuggestion(text);
		textField.setText(text);
		surroundingChars[0] += text;
		surroundingChars = autoCorrectSpace(text, surroundingChars, true, -1);

		if (surroundingChars[0].endsWith(Characters.getSpace(mLanguage))) {
			waitForSpaceTrimKey();
		}

		forceShowWindow();
		updateShiftState(surroundingChars[0], true, false);
		guessNextWord(surroundingChars, lastWord, true);

		return true;
	}


	@NonNull
	protected String[] autoCorrectSpace(@Nullable String currentWord, @NonNull String[] surroundingChars, boolean isWordAcceptedManually, int nextKey) {
		if (currentWord == null || currentWord.isEmpty() || !settings.isAutoAssistanceOn(mInputMode)) {
			return surroundingChars;
		}

		String previousChars = surroundingChars[0];
		final String nextChars = surroundingChars[1];

		if (!inputType.isRustDesk() && mInputMode.shouldDeletePrecedingSpace(previousChars)) {
			textField.deletePrecedingSpace(currentWord);
			if (previousChars.endsWith(" " + currentWord) && previousChars.length() > currentWord.length()) {
				final int precedingSpace = previousChars.length() - currentWord.length() - 1;
				previousChars = previousChars.substring(0, precedingSpace) + currentWord;
			}
		}

		if (mInputMode.shouldAddPrecedingSpace(previousChars)) {
			textField.addPrecedingSpace(currentWord);
			if (previousChars.endsWith(currentWord)) {
				final int startOfWord = previousChars.length() - currentWord.length();
				previousChars = previousChars.substring(0, startOfWord) + " " + currentWord;
			}
		}

		if (mInputMode.shouldAddTrailingSpace(previousChars, nextChars, isWordAcceptedManually, nextKey)) {
			textField.setText(" ");
			previousChars += " ";
		}

		return new String[] { previousChars, nextChars };
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

		textField.deleteChars(mLanguage, charsToDelete);
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
		}

		if (!mLanguage.hasABC()) {
			allowedInputModes.remove((Integer) InputMode.MODE_ABC);
		}

		if (!settings.getPredictiveMode()) {
			allowedInputModes.remove((Integer) InputMode.MODE_PREDICTIVE);
		}

		return InputModeValidator.validateMode(settings.getInputMode(), allowedInputModes);
	}


	/**
	 * In Japanese, Hiragana and Katakana modes are the equivalents of ABC mode in other languages.
	 * So when typing letters is possible (ABC mode allowed), we replace ABC with these two modes.
	 */
	private void determineJapaneseInputModes() {
		if (allowedInputModes.contains(InputMode.MODE_ABC)) {
			allowedInputModes.add(InputMode.MODE_HIRAGANA);
			allowedInputModes.add(InputMode.MODE_KATAKANA);
		}
	}


	/**
	 * determineInputMode
	 * Same as determineInputModeId(), but returns an actual InputMode.
	 */
	protected InputMode determineInputMode() {
		return InputMode.getInstance(settings, mLanguage, inputType, textField, determineInputModeId());
	}


	/**
	 * Try to recompose the current word after a backspace operation. If successful, load new
	 * suggestions. Otherwise, reset the InputMode.
	 */
	private void recompose(int backspaceRepeat, boolean isTextSelected) {
		if (!settings.getBackspaceRecomposing() || backspaceRepeat > 0 || isFnPanelVisible() || isTextSelected || !suggestionOps.isEmpty() || DictionaryLoader.getInstance(this).isRunning()) {
			return;
		}

		final String previousWord = mInputMode.recompose();
		if (textField.recompose(previousWord)) {
			getSuggestions(previousWord, null);
		} else {
			mInputMode.reset();
		}
	}


	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
//		Logger.d("onUpdateSelection", "old (" + oldSelStart + ", " + oldSelEnd + ") => new (" + newSelStart + ", " + newSelEnd + "); candidates = (" + candidatesStart + ", " + candidatesEnd + ")");

		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
		textSelection.onSelectionUpdate(newSelStart, newSelEnd);

		// in case the app has modified the InputField and moved the cursor without notifying us...
		if (appHacks.onUpdateSelection(mInputMode, suggestionOps, oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)) {
			stopWaitingForSpaceTrimKey();
			return;
		}

		// If the cursor moves while composing a word (usually, because the user has touched the screen outside the word), we must
		// end typing end accept the word. Otherwise, the cursor would jump back at the end of the word, after the next key press.
		// This is confusing from user perspective, so we want to avoid it.
		if (CursorOps.isMovedWhileTyping(newSelStart, newSelEnd, candidatesStart, candidatesEnd)) {
			stopWaitingForSpaceTrimKey();
			mInputMode.onCursorMove(suggestionOps.acceptIncomplete());
			return;
		}

		// Prevent deleting a space using the left arrow key, if the user has moved the cursor to another
		// location. This prevents undesired deletion of the space, in the middle of the text.
		if (CursorOps.isMovedFar(newSelStart, newSelEnd, oldSelStart, oldSelEnd)) {
			stopWaitingForSpaceTrimKey();
			clearGuessingContext();
		}
	}


	protected void scrollSuggestions(boolean backward) {
		suggestionOps.cancelDelayedAccept();
		suggestionOps.scrollTo(backward ? -1 : 1);
		mInputMode.setWordStem(suggestionOps.getCurrent(), true);
		if (InputModeKind.isRecomposing(mInputMode)) {
			appHacks.setComposingTextPartsWithHighlightedJoining(mInputMode.getWordStem() + suggestionOps.getCurrent(), mInputMode.getRecomposingSuffix());
		} else {
			appHacks.setComposingTextWithHighlightedStem(suggestionOps.getCurrent(), mInputMode.getWordStem(), mInputMode.isStemFilterFuzzy());
		}
	}


	protected void updateShiftStateDebounced(@Nullable String beforeCursor, boolean determineTextCase, boolean onlyWhenLetters) {
		if (shiftStateDebounceHandler == null) {
			shiftStateDebounceHandler = new Handler(Looper.getMainLooper());
		} else {
			shiftStateDebounceHandler.removeCallbacksAndMessages(null);
		}
		shiftStateDebounceHandler.postDelayed(() -> updateShiftState(beforeCursor, determineTextCase, onlyWhenLetters), SettingsStore.SHIFT_STATE_DEBOUNCE_TIME);
	}


	protected void updateShiftState(@Nullable String beforeCursor, boolean determineTextCase, boolean onlyWhenLetters) {
		if (onlyWhenLetters && !new Text(suggestionOps.getCurrent()).isAlphabetic()) {
			return;
		}

		if (determineTextCase) {
			beforeCursor = beforeCursor != null ? beforeCursor : textField.getStringBeforeCursor();
			mInputMode.determineNextWordTextCase(beforeCursor, -1);
		}

		getDisplayTextCase(mLanguage, mInputMode.getTextCase());
		setStatusIcon(mInputMode, mLanguage);
		mainView.renderDynamicKeys();
		if (!mainView.isTextEditingPaletteShown() && !mainView.isCommandPaletteShown()) {
			statusBar.setText(mInputMode);
		}
	}
}
