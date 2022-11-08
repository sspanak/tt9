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
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.ui.SuggestionsView;
import io.github.sspanak.tt9.ui.UI;

public class TraditionalT9 extends KeyPadHandler {
	private static TraditionalT9 self;

	// input mode
	private ArrayList<Integer> allowedInputModes = new ArrayList<>();
	private InputMode mInputMode;

	// language
	protected ArrayList<Integer> mEnabledLanguages;
	protected Language mLanguage;

	// soft key view
	private SoftKeyHandler softKeyHandler = null;
	private SuggestionsView mSuggestionView = null;


	public static Context getMainContext() {
		return self.getApplicationContext();
	}


	private void loadSettings() {
		mLanguage = LanguageCollection.getLanguage(settings.getInputLanguage());
		mEnabledLanguages = settings.getEnabledLanguageIds();
		mInputMode = InputMode.getInstance(settings.getInputMode());
		mInputMode.setTextCase(settings.getTextCase());
	}


	private void validateLanguages() {
		mEnabledLanguages = InputModeValidator.validateEnabledLanguages(settings, mEnabledLanguages);
		mLanguage = InputModeValidator.validateLanguage(settings, mLanguage, mEnabledLanguages);
	}


	private void validateFunctionKeys() {
		if (!settings.areFunctionKeysSet()) {
			settings.setDefaultKeys();
		}
	}


	protected void onInit() {
		self = this;

		if (softKeyHandler == null) {
			softKeyHandler = new SoftKeyHandler(this);
		}

		if (mSuggestionView == null) {
			mSuggestionView = new SuggestionsView(softKeyHandler.getView());
		}

		loadSettings();
		validateFunctionKeys();
		settings.clearLastWord();
	}


	protected void onRestart(EditorInfo inputField) {
		// in case we are back from Settings screen, update the language list
		mEnabledLanguages = settings.getEnabledLanguageIds();
		validateLanguages();

		// some input fields support only numbers or do not accept predictions
		determineAllowedInputModes(inputField);
		mInputMode = InputModeValidator.validateMode(settings, mInputMode, allowedInputModes);

		// Some modes may want to change the default text case based on grammar rules.
		determineNextTextCase();
		InputModeValidator.validateTextCase(settings, mInputMode, settings.getTextCase());

		// build the UI
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

		restoreAddedWordIfAny();
	}


	protected void onFinish() {
		clearSuggestions();

		hideStatusIcon();
		hideWindow();

		softKeyHandler.hide();
	}


	public boolean onBackspace() {
		if (!InputFieldHelper.isThereText(currentInputConnection)) {
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
			return sendDefaultEditorAction(false);
		}

		mInputMode.onAcceptSuggestion(mLanguage, mSuggestionView.getCurrentSuggestion());
		commitCurrentSuggestion();
		resetKeyRepeat();

		return true;
	}


	protected boolean onUp() {
		if (previousSuggestion()) {
			mInputMode.setWordStem(mLanguage, mSuggestionView.getCurrentSuggestion(), true);
			setComposingTextWithWordStemIndication(mSuggestionView.getCurrentSuggestion());
			return true;
		}

		return false;
	}


	protected boolean onDown() {
		if (nextSuggestion()) {
			mInputMode.setWordStem(mLanguage, mSuggestionView.getCurrentSuggestion(), true);
			setComposingTextWithWordStemIndication(mSuggestionView.getCurrentSuggestion());
			return true;
		}

		return false;
	}


	protected boolean onLeft() {
		if (mInputMode.clearWordStem()) {
			mInputMode.loadSuggestions(handleSuggestionsAsync, mLanguage, getComposingText());
		} else {
			jumpBeforeComposingText();
		}

		return true;
	}


	protected boolean onRight(boolean repeat) {
		String filter = repeat ? mSuggestionView.getSuggestion(1) : getComposingText();

		if (mInputMode.setWordStem(mLanguage, filter, repeat)) {
			mInputMode.loadSuggestions(handleSuggestionsAsync, mLanguage, filter);
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
	protected boolean onNumber(int key, boolean hold, boolean repeat) {
		if (mInputMode.shouldAcceptCurrentSuggestion(mLanguage, key, hold, repeat)) {
			mInputMode.onAcceptSuggestion(mLanguage, getComposingText());
			commitCurrentSuggestion(false);
		}

		// Auto-adjust the text case before each word, if the InputMode supports it.
		// We don't do it too often, because it is somewhat resource-intensive.
		if (getComposingText().length() == 0) {
			determineNextTextCase();
		}

		if (!mInputMode.onNumber(mLanguage, key, hold, repeat)) {
			return false;
		}

		if (mInputMode.shouldSelectNextSuggestion() && !isSuggestionViewHidden()) {
			nextSuggestion();
			return true;
		}

		if (mInputMode.getWord() != null) {
			commitText(mInputMode.getWord());
		} else {
			getSuggestions();
		}

		return true;
	}


	protected boolean onPound() {
		commitText("#");
		return true;
	}


	protected boolean onStar() {
		commitText("*");
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
		if (mEditing == EDITING_DIALER) {
			return false;
		}

		nextLang();
		return true;
	}


	protected boolean onKeyNextInputMode() {
		if (mEditing == EDITING_DIALER) {
			return false;
		}

		nextInputMode();
		return true;
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
		return mSuggestionView == null || !mSuggestionView.isShown();
	}


	private boolean previousSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		mSuggestionView.scrollToSuggestion(-1);
		setComposingTextWithWordStemIndication(mSuggestionView.getCurrentSuggestion());

		return true;
	}


	private boolean nextSuggestion() {
		if (isSuggestionViewHidden()) {
			return false;
		}

		mSuggestionView.scrollToSuggestion(1);
		setComposingTextWithWordStemIndication(mSuggestionView.getCurrentSuggestion());

		return true;
	}


	private void commitCurrentSuggestion() {
		commitCurrentSuggestion(true);
	}

	private void commitCurrentSuggestion(boolean entireSuggestion) {
		if (!isSuggestionViewHidden() && currentInputConnection != null) {
			if (entireSuggestion) {
				setComposingText(mSuggestionView.getCurrentSuggestion());
			}
			currentInputConnection.finishComposingText();
		}

		setSuggestions(null);
	}


	private void clearSuggestions() {
		setSuggestions(null);

		if (currentInputConnection != null) {
			setComposingText("");
			currentInputConnection.finishComposingText();
		}
	}


	private void getSuggestions() {
		if (!mInputMode.loadSuggestions(handleSuggestionsAsync, mLanguage, mSuggestionView.getCurrentSuggestion())) {
			handleSuggestions();
		}
	}


	private void handleSuggestions() {
		setSuggestions(mInputMode.getSuggestions(mLanguage));

		// Put the first suggestion in the text field,
		// but cut it off to the length of the sequence (how many keys were pressed),
		// for a more intuitive experience.
		String word = mSuggestionView.getCurrentSuggestion();
		word = word.substring(0, Math.min(mInputMode.getSequenceLength(), word.length()));
		setComposingTextWithWordStemIndication(word);
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


	private void commitText(String text) {
		if (text != null && currentInputConnection != null) {
			currentInputConnection.commitText(text, 1);
		}
	}


	private String getComposingText() {
		String text = mSuggestionView.getCurrentSuggestion();
		if (text.length() > 0 && text.length() > mInputMode.getSequenceLength()) {
			text = text.substring(0, mInputMode.getSequenceLength());
		}

		return text;
	}


	private void setComposingText(CharSequence text) {
		if (text != null && currentInputConnection != null) {
			currentInputConnection.setComposingText(text, 1);
		}
	}


	private void setComposingTextWithWordStemIndication(CharSequence word) {
		if (mInputMode.getWordStem().length() > 0) {
			setComposingText(TextHelper.highlightComposingText(word, 0, mInputMode.getWordStem().length()));
		} else {
			setComposingText(word);
		}
	}


	private void refreshComposingText() {
		setComposingText(getComposingText());
	}


	private void nextInputMode() {
		if (mEditing == EDITING_STRICT_NUMERIC || mEditing == EDITING_DIALER) {
			clearSuggestions();
			mInputMode = InputMode.getInstance(InputMode.MODE_123);
		}
		// when typing a word or viewing scrolling the suggestions, only change the case
		else if (!isSuggestionViewHidden()) {
			mInputMode.nextTextCase();
			setSuggestions(mInputMode.getSuggestions(mLanguage), mSuggestionView.getCurrentIndex());
			refreshComposingText();
		}
		// make "abc" and "ABC" separate modes from user perspective
		else if (mInputMode.isABC() && mInputMode.getTextCase() == InputMode.CASE_LOWER) {
			mInputMode.setTextCase(InputMode.CASE_UPPER);
		} else {
			int modeIndex = (allowedInputModes.indexOf(mInputMode.getId()) + 1) % allowedInputModes.size();
			mInputMode = InputMode.getInstance(allowedInputModes.get(modeIndex));

			mInputMode.defaultTextCase();
		}

		// save the settings for the next time
		settings.saveInputMode(mInputMode);
		settings.saveTextCase(mInputMode.getTextCase());

		UI.updateStatusIcon(this, mLanguage, mInputMode);
	}


	private void nextLang() {
		if (mEditing == EDITING_STRICT_NUMERIC || mEditing == EDITING_DIALER) {
			return;
		}

		clearSuggestions();

		// select the next language
		int previousLangId = mEnabledLanguages.indexOf(mLanguage.getId());
		int nextLangId = previousLangId == -1 ? 0 : (previousLangId + 1) % mEnabledLanguages.size();
		mLanguage = LanguageCollection.getLanguage(mEnabledLanguages.get(nextLangId));

		validateLanguages();

		// save it for the next time
		settings.saveInputLanguage(mLanguage.getId());

		UI.updateStatusIcon(this, mLanguage, mInputMode);
	}


	private void jumpBeforeComposingText() {
		if (currentInputConnection != null) {
			currentInputConnection.setComposingText(getComposingText(), 0);
			currentInputConnection.finishComposingText();
		}

		setSuggestions(null);
		mInputMode.reset();
	}


	private void determineAllowedInputModes(EditorInfo inputField) {
		allowedInputModes = InputFieldHelper.determineInputModes(inputField);

		int lastInputModeId = settings.getInputMode();
		if (allowedInputModes.contains(lastInputModeId)) {
			mInputMode = InputMode.getInstance(lastInputModeId);
		} else if (allowedInputModes.contains(InputMode.MODE_ABC)) {
			mInputMode = InputMode.getInstance(InputMode.MODE_ABC);
		} else {
			mInputMode = InputMode.getInstance(allowedInputModes.get(0));
		}

		if (InputFieldHelper.isDialerField(inputField)) {
			mEditing = EDITING_DIALER;
		} else if (mInputMode.is123() && allowedInputModes.size() == 1) {
			mEditing = EDITING_STRICT_NUMERIC;
		} else {
			mEditing = InputFieldHelper.isFilterTextField(inputField) ? EDITING_NOSHOW : EDITING;
		}
	}


	private void determineNextTextCase() {
		mInputMode.determineNextWordTextCase(
			InputFieldHelper.isThereText(currentInputConnection),
			(String) currentInputConnection.getTextBeforeCursor(50, 0)
		);
	}


	private void showAddWord() {
		if (currentInputConnection == null) {
			return;
		}

		currentInputConnection.finishComposingText();
		clearSuggestions();

		UI.showAddWordDialog(this, mLanguage.getId(), InputFieldHelper.getSurroundingWord(currentInputConnection));
	}


	/**
	 * restoreAddedWordIfAny
	 * If a new word was added to the dictionary, this function will append add it to the current input field.
	 */
	private void restoreAddedWordIfAny() {
		String word = settings.getLastWord();
		settings.clearLastWord();

		if (word.length() == 0 || word.equals(InputFieldHelper.getSurroundingWord(currentInputConnection))) {
			return;
		}

		try {
			Logger.d("restoreAddedWordIfAny", "Restoring word: '" + word + "'...");
			commitText(word);
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
