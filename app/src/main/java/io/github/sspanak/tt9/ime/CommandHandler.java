package io.github.sspanak.tt9.ime;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdCommandPalette;
import io.github.sspanak.tt9.commands.Command;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Ternary;

abstract public class CommandHandler extends TextEditingHandler {
	private final CmdCommandPalette cmdPalette = new CmdCommandPalette();


	@Override
	protected Ternary onBack() {
		if (cmdPalette.hideCommandPalette(getFinalContext())) {
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
			Command cmd = CommandCollection.getByHardKey(CommandCollection.COLLECTION_PALETTE, key);
			if (cmd.isAvailable(getFinalContext())) {
				cmd.run(getFinalContext());
			}
			return true;
		}

		return super.onNumber(key, hold, repeat);
	}


	@Override
	protected boolean navigateBack() {
		return cmdPalette.hideCommandPalette(getFinalContext()) || super.navigateBack();
	}


	public void resetStatus() {
		if (mainView.isCommandPaletteShown()) {
			statusBar.setText(R.string.commands_select_command);
			statusBar.setAccessibilityText(R.string.commands_select_command);
		} else if (mainView.isTextEditingPaletteShown()) {
			statusBar.setText(R.string.commands_select_command);
			statusBar.setAccessibilityText(R.string.commands_select_command);
		} else {
			statusBar.setText(mInputMode);
			statusBar.setAccessibilityText(mInputMode);
		}
	}


	public void setInputMode(int modeId) {
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
		statusBar.setAccessibilityText(mInputMode);
		mainView.render();

		if (settings.isMainLayoutStealth() && !settings.isStatusIconEnabled()) {
			UI.toastShortSingle(this, mInputMode.getClass().getSimpleName(), mInputMode.toString());
		}
	}


	public void setLang(int langId) {
		if (!mEnabledLanguages.contains(langId)) {
			return;
		}

		suggestionOps.cancelDelayedAccept();
		stopVoiceInput();

		mLanguage = LanguageCollection.getLanguage(langId);
		validateLanguages();

		settings.setDefaultChars(mLanguage, false); // initialize default order, if missing
		((NaturalLanguage) mLanguage).updateKeyCharacters(settings); // and update the layout for 2..9 keys, if needed

		// for languages that do not have ABC or Predictive, make sure we remain in valid state
		mInputMode = InputMode
			.getInstance(settings, mLanguage, inputType, textField, determineInputModeId())
			.copy(mInputMode);

		if (mInputMode.isTyping()) {
			getSuggestions(0, null, this::onAfterLanguageChange);
		} else {
			onAfterLanguageChange();
		}

		if (InputModeKind.isPredictive(mInputMode)) {
			DictionaryLoader.autoLoad(this, settings, mLanguage);
		}

		mindReader.setLanguage(mLanguage).seed(getFinalContext(), mLanguage);

		forceShowWindow();
	}


	private void onAfterLanguageChange() {
		getDisplayTextCase(mLanguage, mInputMode.getTextCase());
		setStatusIcon(mInputMode, mLanguage);
		statusBar.setText(mInputMode);
		statusBar.setAccessibilityText(mInputMode);
		suggestionOps.setLanguage(mLanguage);
		mainView.render();
		if (settings.isMainLayoutStealth() && !settings.isStatusIconEnabled()) {
			UI.toastShortSingle(this, mInputMode.getClass().getSimpleName(), mInputMode.toString());
		}
	}


	public boolean nextTextCase() {
		final String currentWord = !suggestionOps.isEmpty() && mInputMode.isTyping() ? suggestionOps.getCurrent() : "";

		if (!mInputMode.nextTextCase(currentWord, displayTextCase)) {
			return false;
		}

		mInputMode.skipNextTextCaseDetection();
		if (!InputModeKind.isRecomposing(mInputMode)) {
			settings.saveTextCase(mInputMode.getTextCase());
		}

		if (currentWord.isEmpty() && !suggestionOps.isEmpty()) {
			// if we have set the suggestions from a different source, e.g. Clipboard or MindReader,
			// they won't be in the InputMode's state, so adjust the list directly, without any specific rules
			suggestionOps.setTextCase(mLanguage, mInputMode.getTextCase());
			appHacks.setComposingText(suggestionOps.getCurrent());
			return true;
		} else if (currentWord.isEmpty() || (currentWord.length() == 1 && !Character.isAlphabetic(currentWord.charAt(0)))) {
			// if there are no suggestions, or they are special chars, we don't need to adjust their text case
			return true;
		}

		// if there are suggestions, we need to adjust their text case to acknowledge the change
		int currentSuggestionIndex = suggestionOps.getCurrentIndex();
		currentSuggestionIndex = suggestionOps.containsStem() ? currentSuggestionIndex - 1 : currentSuggestionIndex;

		suggestionOps.set(mInputMode.getSuggestions(), currentSuggestionIndex, mInputMode.containsGeneratedSuggestions());

		if (InputModeKind.isRecomposing(mInputMode)) {
			appHacks.setComposingTextPartsWithHighlightedJoining(mInputMode.getWordStem() + suggestionOps.getCurrent(), mInputMode.getRecomposingSuffix());
		} else {
			mindReader.setTextCase(mInputMode.getTextCaseRaw());
			suggestionOps.addGuesses(mindReader.getGuesses());
			appHacks.setComposingText(suggestionOps.getCurrent());
		}

		return true;
	}
}
