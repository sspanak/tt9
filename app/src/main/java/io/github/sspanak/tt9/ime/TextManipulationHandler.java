package io.github.sspanak.tt9.ime;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Ternary;

abstract public class TextManipulationHandler extends VoiceHandler {
	protected boolean onNumber(int key, boolean hold, int repeat) {
		if (!shouldBeOff() && mainView.isTextManipulationPaletteShown()) {
			onCommand(key);
			return true;
		}

		return super.onNumber(key, hold, repeat);
	}


	@Override
	protected Ternary onBack() {
		if (hideTextManipulationPalette()) {
			return Ternary.TRUE;
		} else {
			return super.onBack();
		}
	}


	private void onCommand(int key) {
		switch (key) {
			case 1:
				Logger.d("TXT", "word left");
				break;
			case 2:
				Logger.d("TXT", "select none");
				break;
			case 3:
				Logger.d("TXT", "word right");
				break;
			case 5:
				Logger.d("TXT", "select all");
				break;
			case 7:
				Logger.d("TXT", "cut");
				break;
			case 8:
				Logger.d("TXT", "copy");
				break;
			case 9:
				Logger.d("TXT", "paste");
				break;
		}
	}


	public void showTextManipulationPalette() {
		if (!mainView.isTextManipulationPaletteShown()) {
			stopVoiceInput();
			mainView.showTextManipulationPalette();
			resetStatus();
		}
	}


	private boolean hideTextManipulationPalette() {
		if (mainView.isTextManipulationPaletteShown()) {
			mainView.showCommandPalette();
			resetStatus();
			return true;
		}

		return false;
	}
}
