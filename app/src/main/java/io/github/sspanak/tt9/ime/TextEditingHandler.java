package io.github.sspanak.tt9.ime;

import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.Ternary;
import io.github.sspanak.tt9.util.sys.Clipboard;

abstract public class TextEditingHandler extends VoiceHandler {
	protected boolean isLanguageRTL;


	@Override
	protected void initTray() {
		super.initTray();
		detectRTL();
		suggestionOps.setLanguage(LanguageCollection.getLanguage(settings.getInputLanguage()));
	}


	protected void detectRTL() {
		isLanguageRTL = LanguageKind.isRTL(
			LanguageCollection.getLanguage(settings.getInputLanguage())
		);
	}


	protected boolean onNumber(int key, boolean hold, int repeat) {
		if (!shouldBeOff() && mainView.isTextEditingPaletteShown()) {
			onCommand(key);
			return true;
		}

		return super.onNumber(key, hold, repeat);
	}


	@Override
	protected Ternary onBack() {
		if (hideTextEditingPalette()) {
			return Ternary.TRUE;
		} else {
			return super.onBack();
		}
	}


	private void onCommand(int key) {
		switch (key) {
			case 0:
				if (!InputModeKind.isNumeric(mInputMode)) {
					onText(" ", false);
				}
				break;
			case 1:
				textSelection.selectNextChar(!isLanguageRTL);
				break;
			case 2:
				textSelection.clear();
				break;
			case 3:
				textSelection.selectNextChar(isLanguageRTL);
				break;
			case 4:
				textSelection.selectNextWord(!isLanguageRTL);
				break;
			case 5:
				textSelection.selectAll();
				break;
			case 6:
				textSelection.selectNextWord(isLanguageRTL);
				break;
			case 7:
				textSelection.cut(textField);
				break;
			case 8:
				textSelection.copy();
				break;
			case 9:
				textSelection.paste(textField);
				break;
		}
	}


	public void showTextEditingPalette() {
		if (inputType.isLimited() || mainView.isTextEditingPaletteShown()) {
			return;
		}

		suggestionOps.cancelDelayedAccept();
		suggestionOps.acceptIncomplete();
		mInputMode.reset();
		stopVoiceInput();

		mainView.showTextEditingPalette();
		Clipboard.setOnChangeListener(this, this::resetStatus);
		resetStatus();
	}


	public boolean hideTextEditingPalette() {
		if (!mainView.isTextEditingPaletteShown()) {
			return false;
		}

		if (settings.isMainLayoutNumpad() || settings.isMainLayoutStealth()) {
			mainView.hideTextEditingPalette();
		} else {
			mainView.showCommandPalette();
		}

		Clipboard.clearListener(this);
		resetStatus();
		return true;
	}
}
