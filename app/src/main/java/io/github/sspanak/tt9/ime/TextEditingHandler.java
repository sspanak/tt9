package io.github.sspanak.tt9.ime;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Ternary;

abstract public class TextEditingHandler extends VoiceHandler {
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
				hideTextEditingPalette();
				return;
			case 1:
				textSelection.selectNextWord(true);
				break;
			case 2:
				textSelection.selectNone();
				break;
			case 3:
				textSelection.selectNextWord(false);
				break;
			case 5:
				textSelection.selectAll();
				break;
			case 7:
				if (textSelection.cut(textField)) {
					resetStatus();
				}
				break;
			case 8:
				if (textSelection.copy()) {
					resetStatus();
				}
				break;
			case 9:
				textSelection.paste(textField);
				break;
		}
	}


	public void showTextEditingPalette() {
		if (inputType.isLimited()) {
			UI.toast(this, R.string.text_editing_not_supported_in_this_app);
		}

		if (!mainView.isTextEditingPaletteShown()) {
			stopVoiceInput();
			mainView.showTextEditingPalette();
			resetStatus();
		}
	}


	private boolean hideTextEditingPalette() {
		if (!mainView.isTextEditingPaletteShown()) {
			return false;
		}

		if (settings.isMainLayoutNumpad() || settings.isMainLayoutStealth()) {
			mainView.hideTextEditingPalette();
		} else {
			mainView.showCommandPalette();
		}

		resetStatus();
		return true;
	}
}
