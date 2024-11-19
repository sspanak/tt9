package io.github.sspanak.tt9.ime;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.Clipboard;
import io.github.sspanak.tt9.util.Ternary;

abstract public class TextEditingHandler extends VoiceHandler {
	protected boolean isSystemRTL;


	@Override
	protected boolean onStart(InputConnection connection, EditorInfo field) {
		isSystemRTL = LanguageKind.isRTL(LanguageCollection.getDefault());
		return super.onStart(connection, field);
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
				if (!mInputMode.isNumeric()) {
					onText(" ", false);
				}
				break;
			case 1:
				textSelection.selectNextChar(!isSystemRTL);
				break;
			case 2:
				textSelection.clear();
				break;
			case 3:
				textSelection.selectNextChar(isSystemRTL);
				break;
			case 4:
				textSelection.selectNextWord(!isSystemRTL);
				break;
			case 5:
				textSelection.selectAll();
				break;
			case 6:
				textSelection.selectNextWord(isSystemRTL);
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

		Clipboard.setOnChangeListener(this, null);
		resetStatus();
		return true;
	}
}
