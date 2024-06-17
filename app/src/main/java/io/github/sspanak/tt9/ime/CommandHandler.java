package io.github.sspanak.tt9.ime;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.DictionaryLoader;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.ModeABC;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.dialogs.AddWordDialog;

abstract public class CommandHandler extends VoiceHandler {
	@Override
	protected boolean onBack() {
		return super.onBack() || hideCommandPalette();
	}


	@Override
	public boolean onBackspace() {
		return hideCommandPalette() || super.onBackspace();
	}


	@Override
	public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly) {
		return mainView.isCommandPaletteShown() && keyCode != settings.getKeyCommandPalette();
	}


	@Override
	protected boolean onNumber(int key, boolean hold, int repeat) {
		if (statusBar.isErrorShown()) {
			resetStatus();
		}

		if (!shouldBeOff() && mainView.isCommandPaletteShown()) {
			onCommand(key);
			return true;
		}

		return super.onNumber(key, hold, repeat);
	}


	private void onCommand(int key) {
		switch (key) {
			case 0:
				changeKeyboard();
				break;
			case 1:
				showSettings();
				break;
			case 2:
				addWord();
				break;
			case 3:
				toggleVoiceInput();
				break;
		}
	}


	protected void resetStatus() {
		if (mainView.isCommandPaletteShown()) {
			statusBar.setText(R.string.commands_select_command);
		} else {
			statusBar.setText(mInputMode);
		}
	}


	public void addWord() {
		if (mInputMode.isNumeric() || voiceInputOps.isListening()) {
			return;
		}

		if (DictionaryLoader.getInstance(this).isRunning()) {
			UI.toastShortSingle(this, R.string.dictionary_loading_please_wait);
			return;
		}

		suggestionOps.cancelDelayedAccept();
		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		mainView.hideCommandPalette();
		resetStatus();

		String word = textField.getSurroundingWord(mLanguage);
		if (word.isEmpty()) {
			UI.toastLong(this, R.string.add_word_no_selection);
		} else {
			AddWordDialog.show(this, mLanguage.getId(), word);
		}
	}


	public void changeKeyboard() {
		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();
		UI.showChangeKeyboardDialog(this);
	}


	protected void nextInputMode() {
		if (mInputMode.isPassthrough() || voiceInputOps.isListening()) {
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
			mInputMode.setTextFieldCase(inputType.determineTextCase());
			mInputMode.determineNextWordTextCase(textField.getStringBeforeCursor());

			resetKeyRepeat();
		}

		// save the settings for the next time
		settings.saveInputMode(mInputMode.getId());
		settings.saveTextCase(mInputMode.getTextCase());

		statusBar.setText(mInputMode);
	}


	protected void nextLang() {
		stopVoiceInput();

		// select the next language
		int previous = mEnabledLanguages.indexOf(mLanguage.getId());
		int next = (previous + 1) % mEnabledLanguages.size();
		mLanguage = LanguageCollection.getLanguage(getApplicationContext(), mEnabledLanguages.get(next));

		// validate and save it for the next time
		validateLanguages();
	}


	protected void nextTextCase() {
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


	public void showSettings() {
		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();
		UI.showSettingsScreen(this);
	}


	public void showCommandPalette() {
		suggestionOps.cancelDelayedAccept();
		suggestionOps.acceptIncomplete();
		mInputMode.reset();

		mainView.showCommandPalette();
		resetStatus();
	}


	public boolean hideCommandPalette() {
		if (!mainView.isCommandPaletteShown()) {
			return false;
		}

		mainView.hideCommandPalette();
		if (voiceInputOps.isListening()) {
			stopVoiceInput();
		} else {
			resetStatus();
		}

		return true;
	}
}
