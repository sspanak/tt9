package io.github.sspanak.tt9.ime;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

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
import io.github.sspanak.tt9.ui.SuggestionsView;
import io.github.sspanak.tt9.ui.UI;

public class TraditionalT9 extends KeyPadHandler {
	// internal settings/data
	private boolean isActive = false;
	private TextField textField;
	private InputType inputType;

	final Handler delayHandler = new Handler(Looper.getMainLooper());
	final Runnable delayAction = () -> commitCurrentSuggestion(false);

	// input mode
	private ArrayList<Integer> allowedInputModes = new ArrayList<>();
	private InputMode mInputMode;

	// language
	protected ArrayList<Integer> mEnabledLanguages;
	protected Language mLanguage;

	// soft key view
	private SoftKeyHandler softKeyHandler = null;
	private SuggestionsView mSuggestionView = null;


	private static TraditionalT9 self;
	public static Context getMainContext() {
		return self.getApplicationContext();
	}


	private void loadSettings() {
		mLanguage = LanguageCollection.getLanguage(settings.getInputLanguage());
		mEnabledLanguages = settings.getEnabledLanguageIds();
		validateLanguages();

		mInputMode = InputMode.getInstance(settings, mLanguage, settings.getInputMode());
		mInputMode.setTextCase(settings.getTextCase());
	}


	private void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(settings, mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(settings, mLanguage, mEnabledLanguages);
	}


	private void validateFunctionKeys() {
		if (settings.isSettingsKeyMissing()) {
			settings.setDefaultKeys();
		}
	}


	protected void onInit() {
		self = this;

		DictionaryDb.init(this);
		DictionaryDb.normalizeWordFrequencies(settings);

		if (softKeyHandler == null) {
			softKeyHandler = new SoftKeyHandler(this);
		}

		if (mSuggestionView == null) {
			mSuggestionView = new SuggestionsView(settings, softKeyHandler.getView());
		}

		loadSettings();
		validateFunctionKeys();
		settings.clearLastWord();
	}


	private void initTyping() {
		// in case we are back from Settings screen, update the language list
		mEnabledLanguages = settings.getEnabledLanguageIds();
		validateLanguages();

		// some input fields support only numbers or are not suited for predictions (e.g. password fields)
		determineAllowedInputModes();
		int modeId = InputModeValidator.validateMode(settings, mInputMode, allowedInputModes);
		mInputMode = InputMode.getInstance(settings, mLanguage, modeId);
		mInputMode.setTextFieldCase(textField.determineTextCase(inputType));

		// Some modes may want to change the default text case based on grammar rules.
		determineNextTextCase();
		InputModeValidator.validateTextCase(settings, mInputMode, settings.getTextCase());
	}


	private void initUi() {
		UI.updateStatusIcon(this, mLanguage, mInputMode);

		clearSuggestions();
		mSuggestionView.setDarkTheme(settings.getDarkTheme());

		softKeyHandler.setDarkTheme(settings.getDarkTheme());
		softKeyHandler.setSoftKeysVisibility(settings.getShowSoftKeys());
		softKeyHandler.show();

		if (!isInputViewShown()) {
			showWindow(true);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && mEditing != EDITING_STRICT_NUMERIC && mEditing != EDITING_DIALER) {
			requestShowSelf(1);
		}
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
		isActive = false;

		hideStatusIcon();
		mEditing = NON_EDIT;
	}


	protected void onStop() {
		onFinishTyping();
		clearSuggestions();
		hideWindow();

		softKeyHandler.hide();
	}


	public boolean onBackspace() {
		// 1. Dialer fields seem to handle backspace on their own and we must ignore it,
		// otherwise, keyDown race condition occur for all keys.
		// 2. Allow the assigned key to function normally, when there is no text (e.g. "Back" navigates back)
		if (mEditing == EDITING_DIALER || !textField.isThereText()) {
			Logger.d("onBackspace", "backspace ignored");
			mInputMode.reset();
			return false;
		}

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


	public boolean onOK() {
		if (isSuggestionViewHidden() && currentInputConnection != null) {
			return performOKAction();
		}

		String word = mSuggestionView.getCurrentSuggestion();

		mInputMode.onAcceptSuggestion(word);
		commitCurrentSuggestion();
		autoCorrectSpace(word, true, -1, false, false);
		resetKeyRepeat();

		return true;
	}


	protected boolean onUp() {
		if (previousSuggestion()) {
			mInputMode.setWordStem(mSuggestionView.getCurrentSuggestion(), true);
			textField.setComposingTextWithHighlightedStem(mSuggestionView.getCurrentSuggestion(), mInputMode);
			return true;
		}

		return false;
	}


	protected boolean onDown() {
		if (nextSuggestion()) {
			mInputMode.setWordStem(mSuggestionView.getCurrentSuggestion(), true);
			textField.setComposingTextWithHighlightedStem(mSuggestionView.getCurrentSuggestion(), mInputMode);
			return true;
		}

		return false;
	}


	protected boolean onLeft() {
		if (mInputMode.clearWordStem()) {
			mInputMode.loadSuggestions(handleSuggestionsAsync, getComposingText());
		} else {
			jumpBeforeComposingText();
		}

		return true;
	}


	protected boolean onRight(boolean repeat) {
		String filter;
		if (repeat && !mSuggestionView.getSuggestion(1).equals("")) {
			filter = mSuggestionView.getSuggestion(1);
		} else {
			filter = getComposingText();
		}

		if (mInputMode.setWordStem(filter, repeat)) {
			mInputMode.loadSuggestions(handleSuggestionsAsync, filter);
		} else if (filter.length() == 0) {
			mInputMode.reset();
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
		String currentWord = getComposingText();

		if(mInputMode.isABC()) {
			delayHandler.removeCallbacks(delayAction);
			delayHandler.postDelayed(delayAction, 800);
		}

		// Automatically accept the current word, when the next one is a space or whatnot,
		// instead of requiring "OK" before that.
		if (mInputMode.shouldAcceptCurrentSuggestion(key, hold, repeat > 0)) {
			mInputMode.onAcceptSuggestion(currentWord);
			commitCurrentSuggestion(false);
			autoCorrectSpace(currentWord, false, key, hold, repeat > 0);
			currentWord = "";
		}

		// Auto-adjust the text case before each word, if the InputMode supports it.
		// We don't do it too often, because it is somewhat resource-intensive.
		if (currentWord.length() == 0) {
			determineNextTextCase();
		}

		if (!mInputMode.onNumber(key, hold, repeat)) {
			return false;
		}

		if (mInputMode.shouldSelectNextSuggestion() && !isSuggestionViewHidden()) {
			nextSuggestion();
			return true;
		}

		if (mInputMode.getWord() != null) {
			currentWord = mInputMode.getWord();

			mInputMode.onAcceptSuggestion(currentWord);
			textField.setText(currentWord);
			clearSuggestions();
			autoCorrectSpace(currentWord, true, key, hold, repeat > 0);
			resetKeyRepeat();
		} else {
			getSuggestions();
		}

		return true;
	}


	protected boolean onPound() {
		textField.setText("#");
		return true;
	}


	protected boolean onStar() {
		textField.setText("*");
		return true;
	}


	protected boolean onKeyAddWord() {
		if (mEditing == EDITING_NOSHOW || mEditing == EDITING_DIALER) {
			return false;
		}

		showAddWord();
		return true;
	}


	protected boolean onKeyNextLanguage() {
		if (nextLang()) {
			commitCurrentSuggestion(false);
			mInputMode.changeLanguage(mLanguage);
			mInputMode.reset();
			resetKeyRepeat();
			clearSuggestions();

			return true;
		}

		return false;
	}


	protected boolean onKeyNextInputMode() {
		nextInputMode();
		return (mEditing != EDITING_STRICT_NUMERIC && mEditing != EDITING_DIALER);
	}


	protected boolean onKeyShowSettings() {
		if (mEditing == EDITING_NOSHOW || mEditing == EDITING_DIALER) {
			return false;
		}

		UI.showSettingsScreen(this);
		return true;
	}


	protected boolean shouldTrackNumPress() {
		return mInputMode.shouldTrackNumPress();
	}


	protected boolean shouldTrackUpDown() {
		return mEditing != EDITING_NOSHOW && !isSuggestionViewHidden() && mInputMode.shouldTrackUpDown();
	}

	protected boolean shouldTrackLeftRight() {
		return mEditing != EDITING_NOSHOW && !isSuggestionViewHidden() && mInputMode.shouldTrackLeftRight();
	}


	private boolean isSuggestionViewHidden() {
		return mSuggestionView == null || !mSuggestionView.hasElements();
	}


	private boolean previousSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		mSuggestionView.scrollToSuggestion(-1);
		textField.setComposingTextWithHighlightedStem(mSuggestionView.getCurrentSuggestion(), mInputMode);

		return true;
	}


	private boolean nextSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		mSuggestionView.scrollToSuggestion(1);
		textField.setComposingTextWithHighlightedStem(mSuggestionView.getCurrentSuggestion(), mInputMode);

		return true;
	}


	private void commitCurrentSuggestion() {
		commitCurrentSuggestion(true);
	}

	private void commitCurrentSuggestion(boolean entireSuggestion) {
		if (!isSuggestionViewHidden() && currentInputConnection != null) {
			if (entireSuggestion) {
				textField.setComposingText(mSuggestionView.getCurrentSuggestion());
			}
			currentInputConnection.finishComposingText();
			if(mInputMode.isABC())
				delayHandler.removeCallbacks(delayAction);
		}

		setSuggestions(null);
	}


	private void clearSuggestions() {
		setSuggestions(null);

		if (currentInputConnection != null) {
			textField.setComposingText("");
			currentInputConnection.finishComposingText();
		}
	}


	private void getSuggestions() {
		if (!mInputMode.loadSuggestions(handleSuggestionsAsync, mSuggestionView.getCurrentSuggestion())) {
			handleSuggestions();
		}
	}


	private void handleSuggestions() {
		setSuggestions(mInputMode.getSuggestions());

		// Put the first suggestion in the text field,
		// but cut it off to the length of the sequence (how many keys were pressed),
		// for a more intuitive experience.
		String word = mSuggestionView.getCurrentSuggestion();
		word = word.substring(0, Math.min(mInputMode.getSequenceLength(), word.length()));
		textField.setComposingTextWithHighlightedStem(word, mInputMode);
	}


	private final Handler handleSuggestionsAsync = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message m) {
			handleSuggestions();
		}
	};


	private void setSuggestions(List<String> suggestions) {
		setSuggestions(suggestions, 0);
	}

	private void setSuggestions(List<String> suggestions, int selectedIndex) {
		if (mSuggestionView == null) {
			return;
		}

		boolean show = suggestions != null && suggestions.size() > 0;

		mSuggestionView.setSuggestions(suggestions, selectedIndex);
		setCandidatesViewShown(show);
	}


	private String getComposingText() {
		String text = mSuggestionView.getCurrentSuggestion();
		if (text.length() > 0 && text.length() > mInputMode.getSequenceLength()) {
			text = text.substring(0, mInputMode.getSequenceLength());
		}

		return text;
	}


	private void refreshComposingText() {
		textField.setComposingText(getComposingText());
	}


	private void nextInputMode() {
		if (mEditing == EDITING_STRICT_NUMERIC || mEditing == EDITING_DIALER) {
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
				setSuggestions(mInputMode.getSuggestions(), mSuggestionView.getCurrentIndex());
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

			mInputMode.defaultTextCase();
		}

		// save the settings for the next time
		settings.saveInputMode(mInputMode.getId());
		settings.saveTextCase(mInputMode.getTextCase());

		UI.updateStatusIcon(this, mLanguage, mInputMode);
	}


	private boolean nextLang() {
		if (mEditing == EDITING_STRICT_NUMERIC || mEditing == EDITING_DIALER) {
			return false;
		}

		// when only one language is enabled, just acknowledge the key was pressed
		if (mEnabledLanguages.size() < 2) {
			return true;
		}

		// select the next language
		int previous = mEnabledLanguages.indexOf(mLanguage.getId());
		int next = (previous + 1) % mEnabledLanguages.size();
		mLanguage = LanguageCollection.getLanguage(mEnabledLanguages.get(next));

		validateLanguages();

		// save it for the next time
		settings.saveInputLanguage(mLanguage.getId());

		UI.updateStatusIcon(this, mLanguage, mInputMode);
		UI.toastInputMode(this, settings, mLanguage, mInputMode);

		return true;
	}


	private void jumpBeforeComposingText() {
		String word = getComposingText();

		textField.setComposingText(word, 0);
		textField.finishComposingText();
		mInputMode.onAcceptSuggestion(word);
		mInputMode.reset();
		setSuggestions(null);
	}


	private void determineAllowedInputModes() {
		allowedInputModes = textField.determineInputModes(inputType);

		int lastInputModeId = settings.getInputMode();
		if (allowedInputModes.contains(lastInputModeId)) {
			mInputMode = InputMode.getInstance(settings, mLanguage, lastInputModeId);
		} else if (allowedInputModes.contains(InputMode.MODE_ABC)) {
			mInputMode = InputMode.getInstance(settings, mLanguage, InputMode.MODE_ABC);
		} else {
			mInputMode = InputMode.getInstance(settings, mLanguage, allowedInputModes.get(0));
		}

		if (inputType.isDialer()) {
			mEditing = EDITING_DIALER;
		} else if (mInputMode.is123() && allowedInputModes.size() == 1) {
			mEditing = EDITING_STRICT_NUMERIC;
		} else {
			mEditing = inputType.isFilter() ? EDITING_NOSHOW : EDITING;
		}
	}


	private void autoCorrectSpace(String currentWord, boolean isWordAcceptedManually, int incomingKey, boolean hold, boolean repeat) {
		if (mInputMode.shouldDeletePrecedingSpace(inputType)) {
			textField.deletePrecedingSpace(currentWord);
		}

		if (mInputMode.shouldAddAutoSpace(inputType, textField, isWordAcceptedManually, incomingKey, hold, repeat)) {
			textField.setText(" ");
		}
	}


	private void determineNextTextCase() {
		mInputMode.determineNextWordTextCase(
			settings,
			textField.isThereText(),
			textField.getTextBeforeCursor()
		);
	}


	private boolean performOKAction() {
		int action = textField.getAction();
		switch (action) {
			case EditorInfo.IME_ACTION_NONE:
				return false;
			case TextField.IME_ACTION_ENTER:
				sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
				return true;
			default:
				return currentInputConnection.performEditorAction(action);
		}
	}


	private void showAddWord() {
		if (currentInputConnection == null) {
			return;
		}

		currentInputConnection.finishComposingText();
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
		return softKeyHandler.getView();
	}
}
