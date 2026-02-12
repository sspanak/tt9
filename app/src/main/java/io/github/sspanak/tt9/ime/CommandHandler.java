package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdAddWord;
import io.github.sspanak.tt9.commands.CmdEditWord;
import io.github.sspanak.tt9.commands.CommandCollection;
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
	private int developerMetaState = 0;
	private boolean awaitingDeveloperComboKey = false;

	@Override
	protected Ternary onBack() {
		if (hideDeveloperCommands() || hideCommandPalette()) {
			return Ternary.TRUE;
		}

		return super.onBack();
	}


	@Override
	protected boolean onNumber(int key, boolean hold, int repeat) {
		if (statusBar.isErrorShown()) {
			resetStatus();
		}

		if (!shouldBeOff() && mainView.isDeveloperCommandsShown()) {
			onDeveloperCommand(key);
			return true;
		}

		if (!shouldBeOff() && awaitingDeveloperComboKey) {
			return sendDeveloperCombination(key, repeat);
		}

		if (!shouldBeOff() && mainView.isCommandPaletteShown()) {
			CommandCollection.getByHardKey(CommandCollection.COLLECTION_PALETTE, key).run(getFinalContext());
			return true;
		}

		return super.onNumber(key, hold, repeat);
	}

	@Override
	public boolean onText(String text, boolean validateOnly) {
		if (mainView.isDeveloperCommandsShown() && "#".equals(text)) {
			if (!validateOnly) {
				awaitingDeveloperComboKey = developerMetaState != 0;
				hideDeveloperCommands();
			}
			return true;
		}

		return super.onText(text, validateOnly);
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
			case 7:
				showDeveloperCommands();
				break;
			case 8:
				selectKeyboard();
				break;
		}
	}


	private void onDeveloperCommand(int key) {
		switch (key) {
			case 1:
				toggleDeveloperMeta(KeyEvent.META_CTRL_ON);
				break;
			case 2:
				toggleDeveloperMeta(KeyEvent.META_ALT_ON);
				break;
			case 3:
				toggleDeveloperMeta(KeyEvent.META_FUNCTION_ON);
				break;
			case 4:
				toggleDeveloperMeta(KeyEvent.META_META_ON);
				break;
			case 5:
				toggleDeveloperMeta(KeyEvent.META_SHIFT_ON);
				break;
			case 6:
				toggleDeveloperMeta(KeyEvent.META_CTRL_LEFT_ON);
				break;
			case 7:
				toggleDeveloperMeta(KeyEvent.META_ALT_LEFT_ON);
				break;
			case 8:
				clearDeveloperModifiers();
				break;
			case 9:
				toggleDeveloperMeta(KeyEvent.META_CAPS_LOCK_ON);
				break;
		}

		mainView.renderKeys();
	}


	private void toggleDeveloperMeta(int metaFlag) {
		developerMetaState = (developerMetaState & metaFlag) == 0 ? (developerMetaState | metaFlag) : (developerMetaState & ~metaFlag);
	}


	private boolean sendDeveloperCombination(int key, int repeat) {
		final int keyCode = resolveDeveloperKeyCode(key, repeat);
		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			return false;
		}

		boolean handled = textField.sendDownUpKeyEvents(keyCode, developerMetaState);
		clearDeveloperModifiers();
		awaitingDeveloperComboKey = false;
		resetStatus();
		return handled;
	}


	private int resolveDeveloperKeyCode(int key, int repeat) {
		if (key < 0 || key > 9 || mLanguage == null) {
			return KeyEvent.KEYCODE_UNKNOWN;
		}

		ArrayList<String> keyChars = mLanguage.getKeyCharacters(key);
		if (keyChars.isEmpty()) {
			return KeyEvent.KEYCODE_UNKNOWN;
		}

		int index = repeat % keyChars.size();
		String keyValue = keyChars.get(index);
		if (keyValue == null || keyValue.isEmpty()) {
			return KeyEvent.KEYCODE_UNKNOWN;
		}

		char keyChar = keyValue.charAt(0);
		if (Character.isLetter(keyChar)) {
			String name = "KEYCODE_" + Character.toUpperCase(keyChar);
			return KeyEvent.keyCodeFromString(name);
		}

		if (Character.isDigit(keyChar)) {
			return KeyEvent.KEYCODE_0 + Character.getNumericValue(keyChar);
		}

		return switch (keyChar) {
			case ' ' -> KeyEvent.KEYCODE_SPACE;
			case '\n' -> KeyEvent.KEYCODE_ENTER;
			default -> KeyEvent.KEYCODE_UNKNOWN;
		};
	}


	@Override
	protected boolean navigateBack() {
		return hideDeveloperCommands() || hideCommandPalette() || super.navigateBack();
	}


	protected void resetStatus() {
		if (mainView.isCommandPaletteShown()) {
			statusBar.setText(R.string.commands_select_command);
		} else if (mainView.isTextEditingPaletteShown()) {
			statusBar.setText(R.string.commands_select_command);
		} else {
			statusBar.setText(mInputMode);
		}

		if (mainView.isTextEditingPaletteShown()) {
			String preview = Clipboard.getPreview(this);
			statusBar.setText(preview.isEmpty() ? getString(R.string.commands_select_command) : "[ \"" + preview + "\" ]");
			return;
		}
		if (mainView.isDeveloperCommandsShown()) {
			statusBar.setText(R.string.developer_select_modifier);
			return;
		}

		statusBar.setText(mInputMode);
	}

	public boolean isDeveloperModifierHeld(int keyNumber) {
		if (!mainView.isDeveloperCommandsShown()) {
			return false;
		}

		return switch (keyNumber) {
			case 1 -> (developerMetaState & KeyEvent.META_CTRL_ON) != 0;
			case 2 -> (developerMetaState & KeyEvent.META_ALT_ON) != 0;
			case 3 -> (developerMetaState & KeyEvent.META_FUNCTION_ON) != 0;
			case 4 -> (developerMetaState & KeyEvent.META_META_ON) != 0;
			case 5 -> (developerMetaState & KeyEvent.META_SHIFT_ON) != 0;
			case 6 -> (developerMetaState & KeyEvent.META_CTRL_LEFT_ON) != 0;
			case 7 -> (developerMetaState & KeyEvent.META_ALT_LEFT_ON) != 0;
			case 9 -> (developerMetaState & KeyEvent.META_CAPS_LOCK_ON) != 0;
			default -> false;
		};
	}

	public boolean isDeveloperCommandsEnabled() {
		return settings.getDeveloperCommandsEnabled();
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
		awaitingDeveloperComboKey = false;

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


	public void showDeveloperCommands() {
		if (!settings.getDeveloperCommandsEnabled() || mainView.isDeveloperCommandsShown()) {
			return;
		}

		suggestionOps.cancelDelayedAccept();
		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		mInputMode.reset();
		awaitingDeveloperComboKey = false;
		mainView.showDeveloperCommands();
		resetStatus();
	}


	public boolean hideDeveloperCommands() {
		if (!mainView.isDeveloperCommandsShown()) {
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


	private void clearDeveloperModifiers() {
		developerMetaState = 0;
		mainView.renderKeys();
	}


	protected boolean undo() {
		return textField.sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, false, true);
	}


	protected boolean redo() {
		return textField.sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, true, true);
	}

	public void showDeveloperCommands() {
		if (mainView.isDeveloperCommandsShown()) {
			return;
		}
		suggestionOps.cancelDelayedAccept();
		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		mInputMode.reset();
		mainView.showDeveloperCommands();
		resetStatus();
	}
	
	public boolean hideDeveloperCommands() {
		if (!mainView.isDeveloperCommandsShown()) {
			return false;
		}
		mainView.showKeyboard();
		if (voiceInputOps.isListening()) {
			stopVoiceInput();
		} else {
			resetStatus();
		}
	}
}
