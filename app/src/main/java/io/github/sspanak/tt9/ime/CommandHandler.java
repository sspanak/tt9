package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.ui.dialogs.AddWordDialog;
import io.github.sspanak.tt9.ui.dialogs.ChangeLanguageDialog;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Ternary;
import io.github.sspanak.tt9.util.sys.Clipboard;
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
	public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly) {
		return mainView.isCommandPaletteShown() && Math.abs(keyCode) != Math.abs(settings.getKeyCommandPalette());
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
				showSettings();
				break;
			case 2:
				addWord();
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
		}
	}


	protected void resetStatus() {
		if (mainView.isCommandPaletteShown()) {
			statusBar.setText(R.string.commands_select_command);
			return;
		}

		if (mainView.isTextEditingPaletteShown()) {
			String preview = Clipboard.getPreview(this);
			statusBar.setText(preview.isEmpty() ? getString(R.string.commands_select_command) : "[ \"" + preview + "\" ]");
			return;
		}

		statusBar.setText(mInputMode);
	}


	public void addWord() {
		if (voiceInputOps.isListening()) {
			return;
		}

		if (mLanguage.isTranscribed()) {
			UI.toastShortSingle(this, R.string.function_add_word_not_available);
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

		new AddWordDialog(getFinalContext(), mLanguage, textField.getSurroundingWord(mLanguage)).show();
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


	protected void nextInputMode() {
		if (InputModeKind.isPassthrough(mInputMode) || voiceInputOps.isListening()) {
			return;
		} else if (allowedInputModes.size() == 1 && allowedInputModes.contains(InputMode.MODE_123)) {
			mInputMode = !InputModeKind.is123(mInputMode) ? InputMode.getInstance(settings, mLanguage, inputType, textField, InputMode.MODE_123) : mInputMode;
		} else {
			suggestionOps.cancelDelayedAccept();
			mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
			resetKeyRepeat();

			int nextModeIndex = (allowedInputModes.indexOf(mInputMode.getId()) + 1) % allowedInputModes.size();
			mInputMode = InputMode.getInstance(settings, mLanguage, inputType, textField, allowedInputModes.get(nextModeIndex));
			determineTextCase();
		}

		settings.saveInputMode(mInputMode.getId());
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

		getSuggestions(null);
		setStatusIcon(mInputMode, mLanguage);
		statusBar.setText(mInputMode);
		suggestionOps.setLanguage(mLanguage);
		mainView.render();
		if (settings.isMainLayoutStealth() && !settings.isStatusIconEnabled()) {
			UI.toastShortSingle(this, mInputMode.getClass().getSimpleName(), mInputMode.toString());
		}

		if (InputModeKind.isPredictive(mInputMode)) {
			DictionaryLoader.autoLoad(this, mLanguage);
		}

		forceShowWindow();
	}


	protected boolean nextTextCase() {
		final String currentWord = suggestionOps.isEmpty() || mInputMode.getSequence().isEmpty() ? "" : suggestionOps.getCurrent();

		if (!mInputMode.nextTextCase(currentWord, statusIconTextCase)) {
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
		textField.setComposingText(suggestionOps.getCurrent());

		return true;
	}


	public void showSettings() {
		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();
		UI.showSettingsScreen(this);
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

		mainView.hideCommandPalette();
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
