package io.github.sspanak.tt9.ime;

import android.view.inputmethod.EditorInfo;

import java.util.LinkedList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Ternary;
import io.github.sspanak.tt9.util.chars.Characters;
import io.github.sspanak.tt9.util.sys.Clipboard;

abstract public class TextEditingHandler extends VoiceHandler {
	protected boolean isLanguageRTL;


	@Override
	protected boolean onStart(EditorInfo field, boolean restarting) {
		detectRTL();
		suggestionOps.setLanguage(LanguageCollection.getLanguage(settings.getInputLanguage()));
		return super.onStart(field, restarting);
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
		if (!hideTextEditingPalette()) {
			return super.navigateBack();
		}

		if (settings.isMainLayoutSmall() || settings.isMainLayoutTray()) {
			mainView.showCommandPalette();
		}

		return true;
	}


	public void cut() {
		if (copy()) {
			suggestionOps.clear();
		}
	}


	public boolean copy() {
		CharSequence selectedText = textSelection.getSelectedText();
		if (selectedText.length() == 0) {
			return false;
		}

		Clipboard.copy(this, selectedText);
		return true;
	}


	public void paste() {
		if (!suggestionOps.isEmpty()) {
			suggestionOps.clear();
			return;
		}

		LinkedList<CharSequence> clips = Clipboard.getAll(this);
		if (clips.isEmpty()) {
			UI.toast(this, R.string.commands_clipboard_is_empty);
			return;
		}

		mInputMode.reset();
		suggestionOps.setClipboardItems(clips);
		appHacks.setComposingTextWithHighlightedStem(suggestionOps.getCurrent(), null, false);
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
		resetStatus();
	}


	public boolean hideTextEditingPalette() {
		if (!mainView.isTextEditingPaletteShown()) {
			return false;
		}

		// paste any selected clipboard item and change its priority
		String word = suggestionOps.acceptCurrent();
		if (Clipboard.contains(word)) {
			Clipboard.copy(this, word);
		}

		mainView.showKeyboard();
		resetStatus();
		return true;
	}
}
