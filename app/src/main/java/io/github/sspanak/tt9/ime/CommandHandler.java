package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdAddWord;
import io.github.sspanak.tt9.commands.CmdEditWord;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ime.modes.ModeRecomposing;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.dialogs.AddWordDialog;
import io.github.sspanak.tt9.ui.dialogs.ChangeLanguageDialog;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Ternary;
import io.github.sspanak.tt9.util.sys.DeviceInfo;
import io.github.sspanak.tt9.util.sys.SystemSettings;

abstract public class CommandHandler extends TextEditingHandler {
	@Override
	protected Ternary onBack() {
		if (hideCommandPalette()) {
			return Ternary.TRUE;
		} else {
			return super.onBack();
		}
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
			case 1:
				addWord();
				break;
			case 2:
				editWord();
				break;
			case 3:
				toggleVoiceInput();
				break;
			case 4:
				undo();
				break;
			case 5:
				showTextEditingPalette();
				break;
			case 6:
				redo();
				break;
			case 8:
				selectKeyboard();
				break;
			case 9:
				showSettings();
				break;
		}
	}


	@Override
	protected boolean navigateBack() {
		return hideCommandPalette() || super.navigateBack();
	}


	protected void resetStatus() {
		if (mainView.isCommandPaletteShown()) {
			statusBar.setText(R.string.commands_select_command);
		} else if (mainView.isTextEditingPaletteShown()) {
			statusBar.setText(R.string.commands_select_command);
		} else {
			statusBar.setText(mInputMode);
		}
	}


	public void addWord() {
		if (!CmdAddWord.validate(getFinalContext(), settings, mLanguage)) {
			return;
		}

		suggestionOps.cancelDelayedAccept();
		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		mainView.showKeyboard();
		resetStatus();

		new AddWordDialog(getFinalContext(), mLanguage, textField.getSurroundingWord(mLanguage)).show();
	}


	protected void editWord() {
		if (!CmdEditWord.validate(getFinalContext(), settings, mLanguage)) {
			return;
		}

		final int previousMode = mInputMode.getId();
		if (previousMode == InputMode.MODE_RECOMPOSING) {
			Logger.d(getClass().getSimpleName(), "Already in recomposing mode. Nothing to do.");
			return;
		}

		String word = suggestionOps.getCurrent(mLanguage, mInputMode.getSequenceLength());
		if (word.isEmpty()) {
			word = textField.recomposeSurroundingWord(mLanguage);
		} else {
			suggestionOps.set(null);
		}

		if (word.isEmpty()) {
			UI.toastShortSingle(this, R.string.edit_word_no_selection);
			return;
		}

		setInputMode(InputMode.MODE_RECOMPOSING);
		if (mInputMode.setWordStem(word, false)) {
			((ModeRecomposing) mInputMode).setOnFinishListener(() -> setInputMode(previousMode));
			getSuggestions("", null);
		} else {
			textField.finishComposingText();
			setInputMode(previousMode);
			UI.toastShortSingle(
				this,
				"edit_word_invalid_characters",
				getString(R.string.edit_word_invalid_characters, word, mLanguage.getName())
			);
		}
	}


	public void selectKeyboard() {
		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();
		UI.showChangeKeyboardDialog(this);
	}


	public void nextKeyboard() {
		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();

		if (DeviceInfo.AT_LEAST_ANDROID_9) {
			switchToPreviousInputMethod();
			return;
		}

		try {
			switchInputMethod(SystemSettings.getPreviousIME(this));
		} catch (Exception e) {
			Logger.d(getClass().getSimpleName(), "Could not switch to previous input method. " + e);
		}
	}


	protected int nextInputMode() {
		if (InputModeKind.isPassthrough(mInputMode) || voiceInputOps.isListening()) {
			return mInputMode.getId();
		}

		if (allowedInputModes.size() == 1 && allowedInputModes.contains(InputMode.MODE_123) && !InputModeKind.is123(mInputMode)) {
			return InputMode.MODE_123;
		} else {
			final int nextModeIndex = (allowedInputModes.indexOf(mInputMode.getId()) + 1) % allowedInputModes.size();
			return allowedInputModes.get(nextModeIndex);
		}
	}


	protected void setInputMode(int modeId) {
		if (!allowedInputModes.contains(modeId) && modeId != InputMode.MODE_RECOMPOSING) {
			return;
		}

		suggestionOps.cancelDelayedAccept();
		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		resetKeyRepeat();

		mInputMode = InputMode.getInstance(settings, mLanguage, inputType, textField, modeId);
		determineTextCase();

		if (modeId != InputMode.MODE_RECOMPOSING) {
			settings.saveInputMode(mInputMode.getId());
		}

		// update the UI
		getDisplayTextCase(mLanguage, mInputMode.getTextCase());
		setStatusIcon(mInputMode, mLanguage);
		statusBar.setText(mInputMode);
		mainView.render();

		if (settings.isMainLayoutStealth() && !settings.isStatusIconEnabled()) {
			UI.toastShortSingle(this, mInputMode.getClass().getSimpleName(), mInputMode.toString());
		}
	}


	protected boolean changeLang() {
		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();
		return new ChangeLanguageDialog(getFinalContext(), this::setLang).show();
	}


	protected void nextLang() {
		int previous = mEnabledLanguages.indexOf(mLanguage.getId());
		int next = (previous + 1) % mEnabledLanguages.size();
		setLang(mEnabledLanguages.get(next));
	}


	public void setLang(int langId) {
		if (!mEnabledLanguages.contains(langId)) {
			return;
		}

		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();

		mLanguage = LanguageCollection.getLanguage(langId);
		validateLanguages();

		detectRTL();
		settings.setDefaultCharOrder(mLanguage, false); // initialize default order, if missing

		// for languages that do not have ABC or Predictive, make sure we remain in valid state
		mInputMode = InputMode
			.getInstance(settings, mLanguage, inputType, textField, determineInputModeId())
			.copy(mInputMode);

		if (mInputMode.isTyping()) {
			getSuggestions(null, this::onAfterLanguageChange);
		} else {
			onAfterLanguageChange();
		}

		if (InputModeKind.isPredictive(mInputMode)) {
			DictionaryLoader.autoLoad(this, settings, mLanguage);
		}

		forceShowWindow();
	}


	private void onAfterLanguageChange() {
		getDisplayTextCase(mLanguage, mInputMode.getTextCase());
		setStatusIcon(mInputMode, mLanguage);
		statusBar.setText(mInputMode);
		suggestionOps.setLanguage(mLanguage);
		mainView.render();
		if (settings.isMainLayoutStealth() && !settings.isStatusIconEnabled()) {
			UI.toastShortSingle(this, mInputMode.getClass().getSimpleName(), mInputMode.toString());
		}
	}


	protected boolean nextTextCase() {
		final String currentWord = !suggestionOps.isEmpty() && mInputMode.isTyping() ? suggestionOps.getCurrent() : "";

		if (!mInputMode.nextTextCase(currentWord, displayTextCase)) {
			return false;
		}

		mInputMode.skipNextTextCaseDetection();
		settings.saveTextCase(mInputMode.getTextCase());

		// if there are no suggestions or they are special chars, we don't need to adjust their text case
		if (currentWord.isEmpty() || (currentWord.length() == 1 && !Character.isAlphabetic(currentWord.charAt(0)))) {
			return true;
		}

		// if there are suggestions, we need to adjust their text case to acknowledge the change
		int currentSuggestionIndex = suggestionOps.getCurrentIndex();
		currentSuggestionIndex = suggestionOps.containsStem() ? currentSuggestionIndex - 1 : currentSuggestionIndex;

		suggestionOps.set(mInputMode.getSuggestions(), currentSuggestionIndex, mInputMode.containsGeneratedSuggestions());

		if (InputModeKind.isRecomposing(mInputMode)) {
			appHacks.setComposingTextPartsWithHighlightedJoining(mInputMode.getWordStem() + suggestionOps.getCurrent(), mInputMode.getRecomposingSuffix());
		} else {
			appHacks.setComposingText(suggestionOps.getCurrent());
		}

		return true;
	}


	public void showSettings() {
		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();
		UI.showSettingsScreen(this, null);
	}


	public void showCommandPalette() {
		if (mainView.isCommandPaletteShown()) {
			return;
		}

		suggestionOps.cancelDelayedAccept();
		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		mInputMode.reset();

		mainView.showCommandPalette();
		resetStatus();
	}


	public boolean hideCommandPalette() {
		if (!mainView.isCommandPaletteShown()) {
			return false;
		}

		mainView.showKeyboard();
		if (voiceInputOps.isListening()) {
			stopVoiceInput();
		} else {
			resetStatus();
		}

		return true;
	}


	protected boolean undo() {
		return textField.sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, false, true);
	}


	protected boolean redo() {
		return textField.sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, true, true);
	}
}
