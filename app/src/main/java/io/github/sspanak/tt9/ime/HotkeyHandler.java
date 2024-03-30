package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.ModeABC;
import io.github.sspanak.tt9.ime.modes.ModePredictive;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.dialogs.AddWordDialog;

public abstract class HotkeyHandler extends TypingHandler {
	private boolean isSystemRTL = false;


	@Override
	protected void onInit() {
		if (settings.areHotkeysInitialized()) {
			Hotkeys.setDefault(settings);
		}
	}


	@Override
	protected void onStart(InputConnection connection, EditorInfo field) {
		super.onStart(connection, field);
		isSystemRTL = LanguageKind.isRTL(LanguageCollection.getDefault(this));
	}


	@Override public boolean onBack() {
		return settings.isMainLayoutNumpad();
	}


	@Override public boolean onOK() {
		suggestionOps.cancelDelayedAccept();

		if (suggestionOps.isEmpty()) {
			int action = textField.getAction();
			return action == TextField.IME_ACTION_ENTER ? appHacks.onEnter() : textField.performAction(action);
		}

		onAcceptSuggestionManually(suggestionOps.acceptCurrent(), KeyEvent.KEYCODE_ENTER);
		return true;
	}


	public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly) {
		if (keyCode == settings.getKeyAddWord()) {
			return onKeyAddWord(validateOnly);
		}

		if (keyCode == settings.getKeyChangeKeyboard()) {
			return onKeyChangeKeyboard(validateOnly);
		}

		if (keyCode == settings.getKeyFilterClear()) {
			return onKeyFilterClear(validateOnly);
		}

		if (keyCode == settings.getKeyFilterSuggestions()) {
			return onKeyFilterSuggestions(validateOnly, repeat);
		}

		if (keyCode == settings.getKeyNextLanguage()) {
			return onKeyNextLanguage(validateOnly);
		}

		if (keyCode == settings.getKeyNextInputMode()) {
			return onKeyNextInputMode(validateOnly);
		}

		if (keyCode == settings.getKeyPreviousSuggestion()) {
			return onKeyScrollSuggestion(validateOnly, true);
		}

		if (keyCode == settings.getKeyNextSuggestion()) {
			return onKeyScrollSuggestion(validateOnly, false);
		}

		if (keyCode == settings.getKeyShowSettings()) {
			return onKeyShowSettings(validateOnly);
		}

		return false;
	}


	public boolean onKeyAddWord(boolean validateOnly) {
		if (!isInputViewShown() || mInputMode.isNumeric()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		if (DictionaryLoader.getInstance(this).isRunning()) {
			UI.toastShortSingle(this, R.string.dictionary_loading_please_wait);
			return true;
		}

		suggestionOps.cancelDelayedAccept();
		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());

		String word = textField.getSurroundingWord(mLanguage);
		if (word.isEmpty()) {
			UI.toastLong(this, R.string.add_word_no_selection);
		} else {
			AddWordDialog.show(this, mLanguage.getId(), word);
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
		if (suggestionOps.isEmpty()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		suggestionOps.cancelDelayedAccept();

		if (mInputMode.clearWordStem()) {
			mInputMode.loadSuggestions(this::getSuggestions, suggestionOps.getCurrent(mInputMode.getSequenceLength()));
			return true;
		}

		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		resetKeyRepeat();

		return true;
	}


	public boolean onKeyFilterSuggestions(boolean validateOnly, boolean repeat) {
		if (suggestionOps.isEmpty()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		suggestionOps.cancelDelayedAccept();

		String filter;
		if (repeat && !suggestionOps.get(1).isEmpty()) {
			filter = suggestionOps.get(1);
		} else {
			filter = suggestionOps.getCurrent(mInputMode.getSequenceLength());
		}

		if (filter.isEmpty()) {
			mInputMode.reset();
		} else if (mInputMode.setWordStem(filter, repeat)) {
			mInputMode.loadSuggestions(super::getSuggestions, filter);
		}

		return true;
	}


	public boolean onKeyScrollSuggestion(boolean validateOnly, boolean backward) {
		if (suggestionOps.isEmpty()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		suggestionOps.cancelDelayedAccept();
		backward = isSystemRTL != backward;
		suggestionOps.scrollTo(backward ? -1 : 1);
		mInputMode.setWordStem(suggestionOps.getCurrent(), true);
		appHacks.setComposingTextWithHighlightedStem(suggestionOps.getCurrent(), mInputMode);
		return true;
	}


	public boolean onKeyNextLanguage(boolean validateOnly) {
		if (mInputMode.isNumeric() || mEnabledLanguages.size() < 2) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		suggestionOps.cancelDelayedAccept();
		nextLang();
		mInputMode.changeLanguage(mLanguage);
		mInputMode.clearWordStem();
		getSuggestions();

		setStatusText(mInputMode.toString());
		renderMainView();
		forceShowWindowIfHidden();
		if (!suggestionOps.isEmpty() || settings.isMainLayoutStealth()) {
			UI.toastShortSingle(this, mLanguage.getClass().getSimpleName(), mLanguage.getName());
		}

		if (mInputMode instanceof ModePredictive) {
			DictionaryLoader.autoLoad(this, mLanguage);
		}

		return true;
	}


	public boolean onKeyNextInputMode(boolean validateOnly) {
		if (allowedInputModes.size() == 1) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		suggestionOps.scheduleDelayedAccept(mInputMode.getAutoAcceptTimeout()); // restart the timer
		nextInputMode();
		renderMainView();
		setStatusIcon(mInputMode.getIcon());
		forceShowWindowIfHidden();

		return true;
	}


	public boolean onKeyShowSettings(boolean validateOnly) {
		if (!isInputViewShown()) {
			return false;
		}

		if (!validateOnly) {
			suggestionOps.cancelDelayedAccept();
			UI.showSettingsScreen(this);
		}

		return true;
	}


	private void nextInputMode() {
		if (mInputMode.isPassthrough()) {
			return;
		} else if (allowedInputModes.size() == 1 && allowedInputModes.contains(InputMode.MODE_123)) {
			mInputMode = !mInputMode.is123() ? InputMode.getInstance(settings, mLanguage, inputType, InputMode.MODE_123) : mInputMode;
		}
		// when typing a word or viewing scrolling the suggestions, only change the case
		else if (!suggestionOps.isEmpty()) {
			nextTextCase();
		}
		// make "abc" and "ABC" separate modes from user perspective
		else if (mInputMode instanceof ModeABC && mLanguage.hasUpperCase() && mInputMode.getTextCase() == InputMode.CASE_LOWER) {
			mInputMode.nextTextCase();
		} else {
			int nextModeIndex = (allowedInputModes.indexOf(mInputMode.getId()) + 1) % allowedInputModes.size();
			mInputMode = InputMode.getInstance(settings, mLanguage, inputType, allowedInputModes.get(nextModeIndex));
			mInputMode.setTextFieldCase(textField.determineTextCase(inputType));
			mInputMode.determineNextWordTextCase(textField.getStringBeforeCursor());

			resetKeyRepeat();
		}

		// save the settings for the next time
		settings.saveInputMode(mInputMode.getId());
		settings.saveTextCase(mInputMode.getTextCase());

		setStatusText(mInputMode.toString());
	}


	protected void nextLang() {
		// select the next language
		int previous = mEnabledLanguages.indexOf(mLanguage.getId());
		int next = (previous + 1) % mEnabledLanguages.size();
		mLanguage = LanguageCollection.getLanguage(getApplicationContext(), mEnabledLanguages.get(next));

		validateLanguages();

		// save it for the next time
		settings.saveInputLanguage(mLanguage.getId());
	}


	private void nextTextCase() {
		String currentSuggestionBefore = suggestionOps.getCurrent();
		int currentSuggestionIndex = suggestionOps.getCurrentIndex();

		// When we are in AUTO mode and the dictionary word is in uppercase,
		// the mode would switch to UPPERCASE, but visually, the word would not change.
		// This is why we retry, until there is a visual change.
		for (int retries = 0; retries < 2 && mInputMode.nextTextCase(); retries++) {
			String currentSuggestionAfter = mInputMode.getSuggestions().size() >= suggestionOps.getCurrentIndex() ? mInputMode.getSuggestions().get(suggestionOps.getCurrentIndex()) : "";
			// If the suggestions are special characters, changing the text case means selecting the
			// next character group. Hence, "before" and "after" are different. Also, if the new suggestion
			// list is shorter, the "before" index may be invalid, so "after" would be empty.
			// In these cases, we scroll to the first one, for consistency.
			if (currentSuggestionAfter.isEmpty() || !currentSuggestionBefore.equalsIgnoreCase(currentSuggestionAfter)) {
				currentSuggestionIndex = 0;
				break;
			}

			// the suggestion list is the same and the text case is different, so let's use it
			if (!currentSuggestionBefore.equals(currentSuggestionAfter)) {
				break;
			}
		}

		suggestionOps.set(mInputMode.getSuggestions(), currentSuggestionIndex);
		textField.setComposingText(suggestionOps.getCurrent());
	}
}
