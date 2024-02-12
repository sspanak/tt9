package io.github.sspanak.tt9.ime;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.WordStoreAsync;
import io.github.sspanak.tt9.ime.helpers.AppHacks;
import io.github.sspanak.tt9.ime.helpers.InputModeValidator;
import io.github.sspanak.tt9.ime.helpers.InputType;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.SettingsStore;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;
import io.github.sspanak.tt9.ui.AddWordAct;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.main.MainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;

public class TraditionalT9 extends KeyPadHandler {
	private InputConnection currentInputConnection = null;
	// internal settings/data
	@NonNull private AppHacks appHacks = new AppHacks(null,null, null, null);
	@NonNull private TextField textField = new TextField(null, null);
	@NonNull private InputType inputType = new InputType(null, null);
	@NonNull private final Handler autoAcceptHandler = new Handler(Looper.getMainLooper());
	@NonNull private final Handler normalizationHandler = new Handler(Looper.getMainLooper());

	// input mode
	private ArrayList<Integer> allowedInputModes = new ArrayList<>();
	@NonNull private InputMode mInputMode = InputMode.getInstance(null, null, null, InputMode.MODE_PASSTHROUGH);

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

	public boolean isInputModeNumeric() {
		return mInputMode.is123();
	}

	public boolean isNumericModeStrict() {
		return mInputMode.is123() && inputType.isNumeric() && !inputType.isPhoneNumber();
	}

	public boolean isNumericModeSigned() {
		return mInputMode.is123() && inputType.isSignedNumber();
	}

	public boolean isInputModePhone() {
		return mInputMode.is123() && inputType.isPhoneNumber();
	}

	public int getTextCase() {
		return mInputMode.getTextCase();
	}


	private void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(getMainContext(), mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(getMainContext(), mLanguage, mEnabledLanguages);

		settings.saveEnabledLanguageIds(mEnabledLanguages);
		settings.saveInputLanguage(mLanguage.getId());
	}


	private void validateFunctionKeys() {
		if (settings.areHotkeysInitialized()) {
			Hotkeys.setDefault(settings);
		}
	}


	/**
	 * getInputMode
	 * Load the last input mode or choose a more appropriate one.
	 * Some input fields support only numbers or are not suited for predictions (e.g. password fields)
	 */
	private InputMode getInputMode() {
		if (!inputType.isValid() || (inputType.isLimited() && !appHacks.isTermux())) {
			return InputMode.getInstance(settings, mLanguage, inputType, InputMode.MODE_PASSTHROUGH);
		}

		allowedInputModes = textField.determineInputModes(inputType);
		int validModeId = InputModeValidator.validateMode(settings.getInputMode(), allowedInputModes);
		return InputMode.getInstance(settings, mLanguage, inputType, validModeId);
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
		mInputMode.determineNextWordTextCase(textField.getTextBeforeCursor());
		InputModeValidator.validateTextCase(mInputMode, settings.getTextCase());
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int result = super.onStartCommand(intent, flags, startId);

		String message = intent != null ? intent.getStringExtra(AddWordAct.INTENT_FILTER) : null;
		if (message != null && !message.isEmpty()) {
			forceShowWindowIfHidden();
			UI.toastLong(self, message);
		}

		return result;
	}


	protected void onInit() {
		self = this;
		Logger.enableDebugLevel(settings.getDebugLogsEnabled());

		WordStoreAsync.init(this);

		if (mainView == null) {
			mainView = new MainView(this);
			initTray();
		}

		validateFunctionKeys();
	}


	protected void setInputField(InputConnection connection, EditorInfo field) {
		currentInputConnection = connection;
		inputType = new InputType(currentInputConnection, field);
		textField = new TextField(currentInputConnection, field);
		appHacks = new AppHacks(settings, connection, field, textField);
	}


	private void initTyping() {
		// in case we are back from Settings screen, update the language list
		mEnabledLanguages = settings.getEnabledLanguageIds();
		mLanguage = LanguageCollection.getLanguage(getMainContext(), settings.getInputLanguage());
		validateLanguages();

		resetKeyRepeat();
		setSuggestions(null);
		mInputMode = getInputMode();
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
		statusBar.setText(mInputMode.toString());
		setDarkTheme();
		mainView.render();
	}


	protected void onStart(InputConnection connection, EditorInfo field) {
		Logger.enableDebugLevel(settings.getDebugLogsEnabled());

		setInputField(connection, field);
		initTyping();

		if (mInputMode.isPassthrough()) {
			// When the input is invalid or simple, let Android handle it.
			onStop();
			updateInputViewShown();
			return;
		}

		normalizationHandler.removeCallbacksAndMessages(null);
		initUi();
		updateInputViewShown();
	}


	protected void onFinishTyping() {
		cancelAutoAccept();
		mInputMode = InputMode.getInstance(null, null, null, InputMode.MODE_PASSTHROUGH);
	}


	protected void onStop() {
		onFinishTyping();
		clearSuggestions();
		statusBar.setText("--");

		normalizationHandler.removeCallbacksAndMessages(null);
		normalizationHandler.postDelayed(WordStoreAsync::normalizeNext, SettingsStore.WORD_NORMALIZATION_DELAY);
	}


	public boolean onBack() {
		return settings.getShowSoftNumpad();
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
			mInputMode.determineNextWordTextCase(textField.getTextBeforeCursor());
		}

		if (!mInputMode.onNumber(key, hold, repeat)) {
			return false;
		}

		if (mInputMode.shouldSelectNextSuggestion() && !isSuggestionViewHidden()) {
			onKeyScrollSuggestion(false, false);
			scheduleAutoAccept(mInputMode.getAutoAcceptTimeout());
		} else {
			getSuggestions();
		}

		return true;
	}


	public boolean onOK() {
		cancelAutoAccept();

		if (isSuggestionViewHidden()) {
			int action = textField.getAction();
			return action == TextField.IME_ACTION_ENTER ? appHacks.onEnter() : textField.performAction(action);
		}

		acceptCurrentSuggestion(KeyEvent.KEYCODE_ENTER);
		return true;
	}


	public boolean onText(String text) { return onText(text, false); }

	public boolean onText(String text, boolean validateOnly) {
		if (mInputMode.shouldIgnoreText(text)) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		cancelAutoAccept();
		forceShowWindowIfHidden();

		// accept the previously typed word (if any)
		autoCorrectSpace(acceptIncompleteSuggestion(), false, -1);

		// "type" and accept the new word
		mInputMode.onAcceptSuggestion(text);
		textField.setText(text);
		autoCorrectSpace(text, true, -1);

		return true;
	}


	public boolean onKeyAddWord(boolean validateOnly) {
		if (!isInputViewShown() || mInputMode.isNumeric()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		cancelAutoAccept();
		acceptIncompleteSuggestion();

		String word = textField.getSurroundingWord(mLanguage);
		if (word.isEmpty()) {
			UI.toastLong(this, R.string.add_word_no_selection);
		} else {
			UI.showAddWordDialog(this, mLanguage.getId(), word);
		}

		return true;
	}


	public boolean onKeyChangeKeyboard(boolean validateOnly) {
		if (!isInputViewShown()) {
			return false;
		}

		if (!validateOnly) {
			UI.showChangeKeyboardDialog(this);
		}

		return true;
	}


	public boolean onKeyFilterClear(boolean validateOnly) {
		if (isSuggestionViewHidden()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		cancelAutoAccept();

		if (mInputMode.clearWordStem()) {
			mInputMode.loadSuggestions(this::getSuggestions, getComposingText());
			return true;
		}

		return false;
	}


	public boolean onKeyFilterSuggestions(boolean validateOnly, boolean repeat) {
		if (isSuggestionViewHidden()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		cancelAutoAccept();

		String filter;
		if (repeat && !suggestionBar.getSuggestion(1).equals("")) {
			filter = suggestionBar.getSuggestion(1);
		} else {
			filter = getComposingText();
		}

		if (filter.isEmpty()) {
			mInputMode.reset();
		} else if (mInputMode.setWordStem(filter, repeat)) {
			mInputMode.loadSuggestions(this::getSuggestions, filter);
		}

		return true;
	}


	public boolean onKeyScrollSuggestion(boolean validateOnly, boolean backward) {
		if (isSuggestionViewHidden()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		cancelAutoAccept();
		suggestionBar.scrollToSuggestion(backward ? -1 : 1);
		mInputMode.setWordStem(suggestionBar.getCurrentSuggestion(), true);
		setComposingTextWithHighlightedStem(suggestionBar.getCurrentSuggestion());
		return true;
	}


	public boolean onKeyNextLanguage(boolean validateOnly) {
		if (mInputMode.isNumeric() || mEnabledLanguages.size() < 2) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		cancelAutoAccept();
		commitCurrentSuggestion(false);
		resetKeyRepeat();
		nextLang();
		mInputMode.changeLanguage(mLanguage);
		mInputMode.reset();

		statusBar.setText(mInputMode.toString());
		mainView.render();
		forceShowWindowIfHidden();

		return true;
	}


	public boolean onKeyNextInputMode(boolean validateOnly) {
		if (allowedInputModes.size() == 1) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		scheduleAutoAccept(mInputMode.getAutoAcceptTimeout()); // restart the timer
		nextInputMode();
		mainView.render();


		forceShowWindowIfHidden();
		return true;
	}


	public boolean onKeyShowSettings(boolean validateOnly) {
		if (!isInputViewShown()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		cancelAutoAccept();
		UI.showSettingsScreen(this);
		return true;
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
			&& !suggestionBar.isEmpty()
		) {
			acceptIncompleteSuggestion();
		}
	}


	private boolean isSuggestionViewHidden() {
		return suggestionBar == null || suggestionBar.isEmpty();
	}


	private boolean scheduleAutoAccept(int delay) {
		cancelAutoAccept();

		if (suggestionBar.isEmpty()) {
			return false;
		}

		if (delay == 0) {
			this.acceptCurrentSuggestion();
			return true;
		} else if (delay > 0) {
			autoAcceptHandler.postDelayed(this::acceptCurrentSuggestion, delay);
		}

		return false;
	}


	private void cancelAutoAccept() {
		autoAcceptHandler.removeCallbacksAndMessages(null);
	}


	private void acceptCurrentSuggestion(int fromKey) {
		String word = suggestionBar.getCurrentSuggestion();
		if (word.isEmpty()) {
			return;
		}

		mInputMode.onAcceptSuggestion(word);
		commitCurrentSuggestion();
		autoCorrectSpace(word, true, fromKey);
		resetKeyRepeat();
	}

	private void acceptCurrentSuggestion() {
		acceptCurrentSuggestion(-1);
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
			mInputMode.determineNextWordTextCase(textField.getTextBeforeCursor());
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
		setComposingTextWithHighlightedStem(word);
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
		if (maxLength == 0 || suggestionBar.isEmpty()) {
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


	private void setComposingTextWithHighlightedStem(@NonNull String word) {
		if (appHacks.setComposingTextWithHighlightedStem(word)) {
			Logger.w("highlightComposingText", "Defective text field detected! Text highlighting disabled.");
		} else {
			textField.setComposingTextWithHighlightedStem(word, mInputMode);
		}
	}


	private void nextInputMode() {
		if (mInputMode.isPassthrough()) {
			return;
		} else if (allowedInputModes.size() == 1 && allowedInputModes.contains(InputMode.MODE_123)) {
			mInputMode = !mInputMode.is123() ? InputMode.getInstance(settings, mLanguage, inputType, InputMode.MODE_123) : mInputMode;
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
			int nextModeIndex = (allowedInputModes.indexOf(mInputMode.getId()) + 1) % allowedInputModes.size();
			mInputMode = InputMode.getInstance(settings, mLanguage, inputType, allowedInputModes.get(nextModeIndex));
			mInputMode.setTextFieldCase(textField.determineTextCase(inputType));
			mInputMode.determineNextWordTextCase(textField.getTextBeforeCursor());

			resetKeyRepeat();
		}

		// save the settings for the next time
		settings.saveInputMode(mInputMode.getId());
		settings.saveTextCase(mInputMode.getTextCase());

		statusBar.setText(mInputMode.toString());
	}


	private void nextLang() {
		// select the next language
		int previous = mEnabledLanguages.indexOf(mLanguage.getId());
		int next = (previous + 1) % mEnabledLanguages.size();
		mLanguage = LanguageCollection.getLanguage(getMainContext(), mEnabledLanguages.get(next));

		validateLanguages();

		// save it for the next time
		settings.saveInputLanguage(mLanguage.getId());
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
		if (mInputMode.isPassthrough() || isInputViewShown()) {
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			requestShowSelf(InputMethodManager.SHOW_IMPLICIT);
		} else {
			showWindow(true);
		}
	}


	@Override
	protected boolean shouldBeVisible() {
		return !getInputMode().isPassthrough();
	}


	@Override
	protected boolean shouldBeOff() {
		return currentInputConnection == null || mInputMode.isPassthrough();
	}
}
