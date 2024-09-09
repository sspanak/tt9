package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;

import io.github.sspanak.tt9.db.words.DictionaryLoader;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.ModePredictive;
import io.github.sspanak.tt9.preferences.helpers.Hotkeys;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Ternary;

public abstract class HotkeyHandler extends CommandHandler {
	@Override
	protected void onInit() {
		super.onInit();
		if (settings.areHotkeysInitialized()) {
			Hotkeys.setDefault(settings);
		}
	}


	@Override
	public Ternary onBack() {
		if (super.onBack() == Ternary.TRUE) {
			return Ternary.TRUE;
		} else if (settings.isMainLayoutNumpad()) {
			return Ternary.ALTERNATIVE;
		} else {
			return Ternary.FALSE;
		}
	}


	@Override public boolean onOK() {
		suggestionOps.cancelDelayedAccept();

		if (!suggestionOps.isEmpty()) {
			onAcceptSuggestionManually(suggestionOps.acceptCurrent(), KeyEvent.KEYCODE_ENTER);
			return true;
		}

		int action = textField.getAction();

		if (action == TextField.IME_ACTION_ENTER) {
			boolean actionPerformed = appHacks.onEnter();
			if (actionPerformed) {
				forceShowWindow();
			}
			return actionPerformed;
		}

		return appHacks.onAction(action) || textField.performAction(action);
	}


	public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly) {
		if (super.onHotkey(keyCode, repeat, validateOnly)) {
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			return false;
		}

		if (keyCode == settings.getKeyAddWord()) {
			return onKeyAddWord(validateOnly);
		}

		if (keyCode == settings.getKeyCommandPalette()) {
			return onKeyCommandPalette(validateOnly);
		}

		if (keyCode == settings.getKeyEditText()) {
			return onKeyEditText(validateOnly);
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

		if (keyCode == settings.getKeySelectKeyboard()) {
			return onKeySelectKeyboard(validateOnly);
		}

		if (keyCode == settings.getKeyShowSettings()) {
			return onKeyShowSettings(validateOnly);
		}

		if (keyCode == settings.getKeyVoiceInput()) {
			return onKeyVoiceInput(validateOnly);
		}

		return false;
	}


	private boolean onKeyAddWord(boolean validateOnly) {
		if (!isInputViewShown() || shouldBeOff()) {
			return false;
		}

		if (!validateOnly) {
			addWord();
		}

		return true;
	}


	public boolean onKeyCommandPalette(boolean validateOnly) {
		if (shouldBeOff()) {
			return false;
		}

		if (validateOnly) {
			return true;
		}

		if (mainView.isCommandPaletteShown()) {
			hideCommandPalette();
		} else {
			showCommandPalette();
			forceShowWindow();
		}

		return true;
	}


	private boolean onKeyEditText(boolean validateOnly) {
		if (!isInputViewShown() || shouldBeOff()) {
			return false;
		}

		if (!validateOnly) {
			showTextEditingPalette();
			forceShowWindow();
		}

		return true;
	}


	public boolean onKeyMoveCursor(boolean backward) {
		if (textSelection.isEmpty()) {
			return appHacks.onMoveCursor(backward) || textField.moveCursor(backward);
		} else {
			textSelection.clear(backward);
			return true;
		}
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
			mInputMode
				.setOnSuggestionsUpdated(this::getSuggestions)
				.loadSuggestions(suggestionOps.getCurrent(mInputMode.getSequenceLength()));
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
			mInputMode
				.setOnSuggestionsUpdated(super::getSuggestions)
				.loadSuggestions(filter);
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

		backward = isSystemRTL != backward;
		scrollSuggestions(backward);

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

		statusBar.setText(mInputMode);
		mainView.render();
		if (!suggestionOps.isEmpty() || settings.isMainLayoutStealth()) {
			UI.toastShortSingle(this, mInputMode.getClass().getSimpleName(), mInputMode.toString());
		}

		if (mInputMode instanceof ModePredictive) {
			DictionaryLoader.autoLoad(this, mLanguage);
		}

		forceShowWindow();
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
		mainView.render();

		if (settings.isMainLayoutStealth()) {
			UI.toastShortSingle(this, mInputMode.getClass().getSimpleName(), mInputMode.toString());
		}

		forceShowWindow();
		return true;
	}


	private boolean onKeySelectKeyboard(boolean validateOnly) {
		if (!isInputViewShown() || shouldBeOff()) {
			return false;
		}

		if (!validateOnly) {
			selectKeyboard();
		}

		return true;
	}

	private boolean onKeyShowSettings(boolean validateOnly) {
		if (!isInputViewShown() || shouldBeOff()) {
			return false;
		}

		if (!validateOnly) {
			showSettings();
		}

		return true;
	}

	private boolean onKeyVoiceInput(boolean validateOnly) {
		if (!isInputViewShown() || shouldBeOff() || !voiceInputOps.isAvailable()) {
			return false;
		}

		if (!validateOnly) {
			toggleVoiceInput();
		}

		return true;
	}
}
