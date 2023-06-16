package io.github.sspanak.tt9.ime;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.db.DictionaryDb;
import io.github.sspanak.tt9.ime.helpers.InputModeValidator;
import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;

public class TraditionalT9 extends KeyPadHandler {
	// internal settings/data
	private boolean isActive = false;
	@NotNull private TextField textField = new TextField(null, null);
	@NotNull private InputType inputType = new InputType(null, null);
	@NotNull private final Handler autoAcceptHandler = new Handler(Looper.getMainLooper());

	// input mode
	private ArrayList<Integer> allowedInputModes = new ArrayList<>();
	private InputMode mInputMode;

	// language
	protected ArrayList<Integer> mEnabledLanguages;
	protected Language mLanguage;

	// soft key view
	private MainView mainView = null;
	private StatusBar statusBar = null;
	private SuggestionsBar suggestionBar = null;


	private static TraditionalT9 self;
	public static Context getMainContext() {
		return self.getApplicationContext();
	}

	public SettingsStore getSettings() {
		return settings;
	}

	public int getInputMode() {
		return mInputMode != null ? mInputMode.getId() : InputMode.MODE_UNDEFINED;
	}

	public int getTextCase() {
		return mInputMode != null ? mInputMode.getTextCase() : InputMode.CASE_UNDEFINED;
	}


	private void loadSettings() {
		mLanguage = LanguageCollection.getLanguage(settings.getInputLanguage());
		mEnabledLanguages = settings.getEnabledLanguageIds();
		validateLanguages();

		mInputMode = InputMode.getInstance(settings, mLanguage, settings.getInputMode());
		mInputMode.setTextCase(settings.getTextCase());
	}


	private void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(mLanguage, mEnabledLanguages);

		settings.saveEnabledLanguageIds(mEnabledLanguages);
		settings.saveInputLanguage(mLanguage.getId());
	}


	private void validateFunctionKeys() {
		if (settings.areHotkeysInitialized()) {
			Hotkeys.setDefault(settings);
		}
	}


	/**
	 * determineInputMode
	 * Restore the last input mode or choose a more appropriate one.
	 * Some input fields support only numbers or are not suited for predictions (e.g. password fields)
	 */
	private void determineInputMode() {
		allowedInputModes = textField.determineInputModes(inputType);
		int validModeId = InputModeValidator.validateMode(settings.getInputMode(), allowedInputModes);
		mInputMode = InputMode.getInstance(settings, mLanguage, validModeId);
	}


	/**
	 * determineTextCase
	 * Restore the last text case or auto-select a new one. If the InputMode supports it, it can change
	 * the text case based on grammar rules, otherwise we fallback to the input field properties or the
	 * last saved mode.
	 */
	private void determineTextCase() {
		mInputMode.setTextFieldCase(textField.determineTextCase(inputType));
		mInputMode.determineNextWordTextCase(textField.isThereText(), textField.getTextBeforeCursor());
		InputModeValidator.validateTextCase(mInputMode, settings.getTextCase());
	}


	protected void onInit() {
		self = this;

		DictionaryDb.init(this);
		DictionaryDb.normalizeWordFrequencies(settings);

		if (mainView == null) {
			mainView = new MainView(this);
			initTray();
		}

		loadSettings();
		validateFunctionKeys();
		settings.clearLastWord();
	}


	private void initTyping() {
		// in case we are back from Settings screen, update the language list
		mEnabledLanguages = settings.getEnabledLanguageIds();
		validateLanguages();

		resetKeyRepeat();
		setSuggestions(null);
		determineInputMode();
		determineTextCase();
	}


	private void initTray() {
		setInputView(mainView.getView());
		statusBar = new StatusBar(mainView.getView());
		suggestionBar = new SuggestionsBar(this, mainView.getView());
	}


	private void setDarkTheme() {
		mainView.setDarkTheme(settings.getDarkTheme());
		statusBar.setDarkTheme(settings.getDarkTheme());
		suggestionBar.setDarkTheme(settings.getDarkTheme());
	}


	private void initUi() {
		if (mainView.createView()) {
			initTray();
		}
		statusBar.setText(mInputMode != null ? mInputMode.toString() : "");
		setDarkTheme();
		mainView.render();
	}


	protected void onStart(EditorInfo input) {
		inputType = new InputType(currentInputConnection, input);
		textField = new TextField(currentInputConnection, input);

		if (!inputType.isValid() || inputType.isLimited()) {
			// When the input is invalid or simple, let Android handle it.
			onStop();
			return;
		}

		initTyping();
		initUi();
		restoreAddedWordIfAny();

		isActive = true;
	}


	protected void onRestart(EditorInfo inputField) {
		if (!isActive) {
			onStart(inputField);
		}
	}


	protected void onFinishTyping() {
		cancelAutoAccept();
		isActive = false;
	}


	protected void onStop() {
		onFinishTyping();
		clearSuggestions();
		statusBar.setText("--");
	}


	public boolean onArrow(int key, boolean repeat) {
		if (key == settings.getKeyFilterClear()) {
			return onKeyFilterClear();
		} else if (key == settings.getKeyFilterSuggestions()) {
			return onKeyFilterSuggestions(repeat);
		} else if (key == settings.getKeyPreviousSuggestion()) {
			return onKeyPreviousSuggestion();
		} else if (key == settings.getKeyNextSuggestion()) {
			return onKeyNextSuggestion();
		}

		return false;
	}


	public boolean onBackspace() {
		// 1. Dialer fields seem to handle backspace on their own and we must ignore it,
		// otherwise, keyDown race condition occur for all keys.
		// 2. Allow the assigned key to function normally, when there is no text (e.g. "Back" navigates back)
		if (mInputMode.isPassthrough() || !textField.isThereText()) {
			Logger.d("onBackspace", "backspace ignored");
			mInputMode.reset();
			return false;
		}

		cancelAutoAccept();
		resetKeyRepeat();

		if (mInputMode.onBackspace()) {
			getSuggestions();
		} else {
			commitCurrentSuggestion(false);
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
		cancelAutoAccept();
		forceShowWindowIfHidden();

		// Automatically accept the previous word, when the next one is a space or punctuation,
		// instead of requiring "OK" before that.
		// First pass, analyze the incoming key press and decide whether it could be the start of
		// a new word.
		if (mInputMode.shouldAcceptPreviousSuggestion(key)) {
			autoCorrectSpace(acceptIncompleteSuggestion(), false, key);
		}

		// Auto-adjust the text case before each word, if the InputMode supports it.
		if (getComposingText().isEmpty()) {
			mInputMode.determineNextWordTextCase(textField.isThereText(), textField.getTextBeforeCursor());
		}

		if (!mInputMode.onNumber(key, hold, repeat)) {
			return false;
		}

		if (mInputMode.shouldSelectNextSuggestion() && !isSuggestionViewHidden()) {
			nextSuggestion();
			scheduleAutoAccept(mInputMode.getAutoAcceptTimeout());
		} else {
			getSuggestions();
		}

		return true;
	}


	public boolean onOK() {
		cancelAutoAccept();

		if (!isInputViewShown() && !textField.isThereText()) {
			forceShowWindowIfHidden();
			return true;
		} else if (isSuggestionViewHidden()) {
			return performOKAction();
		}

		String word = suggestionBar.getCurrentSuggestion();

		mInputMode.onAcceptSuggestion(word);
		commitCurrentSuggestion();
		autoCorrectSpace(word, true, KeyEvent.KEYCODE_ENTER);
		resetKeyRepeat();

		return true;
	}


	public boolean onOtherKey(int keyCode) {
		cancelAutoAccept();

		String acceptedWord = acceptIncompleteSuggestion();
		if (mInputMode.onOtherKey(keyCode)) {
			autoCorrectSpace(acceptedWord, false, keyCode);
			getSuggestions();
			resetKeyRepeat();
			return true;
		}

		return acceptedWord.length() > 0;
	}


	public boolean onText(String text) {
		if (mInputMode.isNumeric() || text.length() == 0) {
			return false;
		}

		cancelAutoAccept();

		// accept the previously typed word (if any)
		autoCorrectSpace(acceptIncompleteSuggestion(), false, -1);

		// "type" and accept the text
		mInputMode.onAcceptSuggestion(text);
		textField.setText(text);
		autoCorrectSpace(text, true, -1);
		return true;
	}


	public boolean onKeyAddWord() {
		if (!isInputViewShown() || mInputMode.isNumeric()) {
			return false;
		}

		cancelAutoAccept();
		showAddWord();
		return true;
	}


	public boolean onKeyFilterClear() {
		if (!suggestionBar.hasElements()) {
			return false;
		}

		cancelAutoAccept();

		if (mInputMode.clearWordStem()) {
			mInputMode.loadSuggestions(this::getSuggestions, getComposingText());
			return true;
		}

		return false;
	}


	public boolean onKeyFilterSuggestions(boolean repeat) {
		if (!suggestionBar.hasElements()) {
			return false;
		}

		cancelAutoAccept();

		String filter;
		if (repeat && !suggestionBar.getSuggestion(1).equals("")) {
			filter = suggestionBar.getSuggestion(1);
		} else {
			filter = getComposingText();
		}

		if (mInputMode.setWordStem(filter, repeat)) {
			mInputMode.loadSuggestions(this::getSuggestions, filter);
		} else if (filter.length() == 0) {
			mInputMode.reset();
		}

		return true;
	}


	public boolean onKeyNextSuggestion() {
		if (nextSuggestion()) {
			cancelAutoAccept();
			mInputMode.setWordStem(suggestionBar.getCurrentSuggestion(), true);
			textField.setComposingTextWithHighlightedStem(suggestionBar.getCurrentSuggestion(), mInputMode);
			return true;
		}

		return false;
	}


	public boolean onKeyPreviousSuggestion() {
		if (previousSuggestion()) {
			cancelAutoAccept();
			mInputMode.setWordStem(suggestionBar.getCurrentSuggestion(), true);
			textField.setComposingTextWithHighlightedStem(suggestionBar.getCurrentSuggestion(), mInputMode);
			return true;
		}

		return false;
	}


	public boolean onKeyNextLanguage() {
		if (nextLang()) {
			cancelAutoAccept();
			commitCurrentSuggestion(false);
			mInputMode.changeLanguage(mLanguage);
			mInputMode.reset();
			resetKeyRepeat();
			clearSuggestions();
			statusBar.setText(mInputMode.toString());
			mainView.render();
			forceShowWindowIfHidden();

			return true;
		}

		return false;
	}


	public boolean onKeyNextInputMode() {
		scheduleAutoAccept(mInputMode.getAutoAcceptTimeout()); // restart the timer
		nextInputMode();
		mainView.render();

		if (allowedInputModes.size() == 1) {
			return false;
		}

		forceShowWindowIfHidden();
		return true;
	}


	public boolean onKeyShowSettings() {
		if (!isInputViewShown()) {
			return false;
		}

		cancelAutoAccept();
		UI.showSettingsScreen(this);
		return true;
	}


	protected boolean shouldTrackUpDown() {
		return !isSuggestionViewHidden() && mInputMode.shouldTrackUpDown();
	}

	protected boolean shouldTrackLeftRight() {
		return !isSuggestionViewHidden() && mInputMode.shouldTrackLeftRight();
	}


	private boolean isSuggestionViewHidden() {
		return suggestionBar == null || !suggestionBar.hasElements();
	}


	private boolean previousSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		suggestionBar.scrollToSuggestion(-1);
		textField.setComposingTextWithHighlightedStem(suggestionBar.getCurrentSuggestion(), mInputMode);

		return true;
	}


	private boolean nextSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		suggestionBar.scrollToSuggestion(1);
		textField.setComposingTextWithHighlightedStem(suggestionBar.getCurrentSuggestion(), mInputMode);

		return true;
	}


	private boolean scheduleAutoAccept(int delay) {
		cancelAutoAccept();

		if (delay == 0) {
			this.onOK();
			return true;
		} else if (delay > 0) {
			autoAcceptHandler.postDelayed(this::autoAccept, delay);
		}

		return false;
	}


	private void cancelAutoAccept() {
		autoAcceptHandler.removeCallbacksAndMessages(null);
	}


	private void autoAccept() {
		if (suggestionBar.hasElements()) {
			this.onOK();
		}
	}


	private String acceptIncompleteSuggestion() {
		String currentWord = getComposingText();
		mInputMode.onAcceptSuggestion(currentWord);
		commitCurrentSuggestion(false);

		return currentWord;
	}


	private void commitCurrentSuggestion() {
		commitCurrentSuggestion(true);
	}

	private void commitCurrentSuggestion(boolean entireSuggestion) {
		if (!isSuggestionViewHidden()) {
			if (entireSuggestion) {
				textField.setComposingText(suggestionBar.getCurrentSuggestion());
			}
			textField.finishComposingText();
		}

		setSuggestions(null);
	}


	private void clearSuggestions() {
		setSuggestions(null);
		textField.setComposingText("");
		textField.finishComposingText();
	}


	private void getSuggestions() {
		mInputMode.loadSuggestions(this::handleSuggestions, suggestionBar.getCurrentSuggestion());
	}


	private void handleSuggestions() {
		// Automatically accept the previous word, without requiring OK. This is similar to what
		// Second pass, analyze the available suggestions and decide if combining them with the
		// last key press makes up a compound word like: (it)'s, (I)'ve, l'(oiseau), or it is
		// just the end of a sentence, like: "word." or "another?"
		if (mInputMode.shouldAcceptPreviousSuggestion()) {
			String lastComposingText = getComposingText(mInputMode.getSequenceLength() - 1);
			commitCurrentSuggestion(false);
			mInputMode.onAcceptSuggestion(lastComposingText, true);
			autoCorrectSpace(lastComposingText, false, -1);
			mInputMode.determineNextWordTextCase(textField.isThereText(), textField.getTextBeforeCursor());
		}

		// key code "suggestions" take priority over words
		if (mInputMode.getKeyCode() > 0) {
			sendDownUpKeyEvents(mInputMode.getKeyCode());
			mInputMode.reset();
			return;
		}

		// display the word suggestions
		setSuggestions(mInputMode.getSuggestions());

		// flush the first suggestion, if the InputMode has requested it
		if (scheduleAutoAccept(mInputMode.getAutoAcceptTimeout())) {
			return;
		}

		// Otherwise, put the first suggestion in the text field,
		// but cut it off to the length of the sequence (how many keys were pressed),
		// for a more intuitive experience.
		String word = suggestionBar.getCurrentSuggestion();
		word = word.substring(0, Math.min(mInputMode.getSequenceLength(), word.length()));
		textField.setComposingTextWithHighlightedStem(word, mInputMode);
	}


	private void setSuggestions(List<String> suggestions) {
		setSuggestions(suggestions, 0);
	}

	private void setSuggestions(List<String> suggestions, int selectedIndex) {
		if (suggestionBar != null) {
			suggestionBar.setSuggestions(suggestions, selectedIndex);
		}
	}


	private String getComposingText(int maxLength) {
		if (maxLength == 0) {
			return "";
		}

		maxLength = maxLength > 0 ? Math.min(maxLength, mInputMode.getSequenceLength()) : mInputMode.getSequenceLength();

		String text = suggestionBar.getCurrentSuggestion();
		if (text.length() > 0 && text.length() > maxLength) {
			text = text.substring(0, maxLength);
		}

		return text;
	}


	private String getComposingText() {
		return getComposingText(-1);
	}


	private void refreshComposingText() {
		textField.setComposingText(getComposingText());
	}


	private void nextInputMode() {
		if (mInputMode.isPassthrough()) {
			return;
		} else if (allowedInputModes.size() == 1 && allowedInputModes.contains(InputMode.MODE_123)) {
			mInputMode = !mInputMode.is123() ? InputMode.getInstance(settings, mLanguage, InputMode.MODE_123) : mInputMode;
		}
		// when typing a word or viewing scrolling the suggestions, only change the case
		else if (!isSuggestionViewHidden()) {
			String currentSuggestionBefore = getComposingText();

			// When we are in AUTO mode and the dictionary word is in uppercase,
			// the mode would switch to UPPERCASE, but visually, the word would not change.
			// This is why we retry, until there is a visual change.
			for (int retries = 0; retries < 2 && mLanguage.hasUpperCase(); retries++) {
				mInputMode.nextTextCase();
				setSuggestions(mInputMode.getSuggestions(), suggestionBar.getCurrentIndex());
				refreshComposingText();

				if (!currentSuggestionBefore.equals(getComposingText())) {
					break;
				}
			}
		}
		// make "abc" and "ABC" separate modes from user perspective
		else if (mInputMode.isABC() && mInputMode.getTextCase() == InputMode.CASE_LOWER && mLanguage.hasUpperCase()) {
			mInputMode.nextTextCase();
		} else {
			int modeIndex = (allowedInputModes.indexOf(mInputMode.getId()) + 1) % allowedInputModes.size();
			mInputMode = InputMode.getInstance(settings, mLanguage, allowedInputModes.get(modeIndex));
			mInputMode.setTextFieldCase(textField.determineTextCase(inputType));
			mInputMode.determineNextWordTextCase(textField.isThereText(), textField.getTextBeforeCursor());

			resetKeyRepeat();
		}

		// save the settings for the next time
		settings.saveInputMode(mInputMode.getId());
		settings.saveTextCase(mInputMode.getTextCase());

		statusBar.setText(mInputMode.toString());
	}


	private boolean nextLang() {
		if (mInputMode.isNumeric() || mEnabledLanguages.size() < 2) {
			return false;
		}

		// select the next language
		int previous = mEnabledLanguages.indexOf(mLanguage.getId());
		int next = (previous + 1) % mEnabledLanguages.size();
		mLanguage = LanguageCollection.getLanguage(mEnabledLanguages.get(next));

		validateLanguages();

		// save it for the next time
		settings.saveInputLanguage(mLanguage.getId());

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


	private boolean performOKAction() {
		if (currentInputConnection == null) {
			return false;
		}

		int action = textField.getAction();
		switch (action) {
			case EditorInfo.IME_ACTION_NONE:
				return false;
			case TextField.IME_ACTION_ENTER:
				String oldText = textField.getTextBeforeCursor() + textField.getTextAfterCursor();

				sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_CENTER);

				try {
					// In Android there is no strictly defined confirmation key, hence DPAD_CENTER may have done nothing.
					// If so, send an alternative key code as a final resort.
					Thread.sleep(80);
					String newText = textField.getTextBeforeCursor() + textField.getTextAfterCursor();
					if (newText.equals(oldText)) {
						sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
					}
				} catch (InterruptedException e) {
					// This thread got interrupted. Assume it's because the connected application has taken an action
					// after receiving DPAD_CENTER, so we don't need to do anything else.
					return true;
				}

				return true;
			default:
				return currentInputConnection.performEditorAction(action);
		}
	}


	private void showAddWord() {
		if (shouldBeOff()) {
			return;
		}

		textField.finishComposingText();
		clearSuggestions();

		UI.showAddWordDialog(this, mLanguage.getId(), textField.getSurroundingWord());
	}


	/**
	 * restoreAddedWordIfAny
	 * If a new word was added to the dictionary, this function will append add it to the current input field.
	 */
	private void restoreAddedWordIfAny() {
		String word = settings.getLastWord();
		settings.clearLastWord();

		if (word.length() == 0 || word.equals(textField.getSurroundingWord())) {
			return;
		}

		try {
			Logger.d("restoreAddedWordIfAny", "Restoring word: '" + word + "'...");
			textField.setText(word);
			mInputMode.reset();
		} catch (Exception e) {
			Logger.w("tt9/restoreLastWord", "Could not restore the last added word. " + e.getMessage());
		}
	}


	/**
	 * createSoftKeyView
	 * Generates the actual UI of TT9.
	 */
	protected View createSoftKeyView() {
		mainView.forceCreateView();
		initTray();
		setDarkTheme();
		return mainView.getView();
	}


	/**
	 * forceShowWindowIfHidden
	 * Some applications may hide our window and it remains invisible until the screen is touched or OK is pressed.
	 * This is fine for touchscreen keyboards, but the hardware keyboard allows typing even when the window and the suggestions
	 * are invisible. This function forces the InputMethodManager to show our window.
	 */
	protected void forceShowWindowIfHidden() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
				&& !mInputMode.isPassthrough()
				&& !isInputViewShown()
		) {
			requestShowSelf(InputMethodManager.SHOW_IMPLICIT);
		}
	}


	@Override
	protected boolean shouldBeVisible() {
		return !mInputMode.isPassthrough() && isActive;
	}


	@Override
	protected boolean shouldBeOff() {
		 return currentInputConnection == null || !isActive || mInputMode.isPassthrough();
	}
}
