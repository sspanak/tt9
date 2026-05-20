package io.github.sspanak.tt9.ime;

import android.view.inputmethod.EditorInfo;

import io.github.sspanak.tt9.commands.CmdEditText;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.util.Ternary;
import io.github.sspanak.tt9.util.chars.Characters;

abstract public class TextEditingHandler extends VoiceHandler {
	@Override
	protected boolean onStart(EditorInfo field, boolean restarting) {
		suggestionOps.setLanguage(LanguageCollection.getLanguage(settings.getInputLanguage()));
		return super.onStart(field, restarting);
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
		if (navigateBack()) {
			return Ternary.TRUE;
		} else {
			return super.onBack();
		}
	}


	private void onCommand(int key) {
		if (!suggestionOps.isEmpty() && key != 9) {
			suggestionOps.acceptCurrent();
		}

		if (key == 0) {
			if (!InputModeKind.isNumeric(mInputMode)) {
				onText(Characters.getSpace(mLanguage), false);
			}
		} else {
			CommandCollection.getByHardKey(CommandCollection.COLLECTION_TEXT_EDITING, key).run(getFinalContext());
		}
	}


	protected boolean navigateBack() {
		if (!new CmdEditText().hideTextEditingPalette(getFinalContext())) {
			return super.navigateBack();
		}

		if (settings.isMainLayoutSmall() || settings.isMainLayoutTray()) {
			mainView.showCommandPalette();
		}

		return true;
	}
}
