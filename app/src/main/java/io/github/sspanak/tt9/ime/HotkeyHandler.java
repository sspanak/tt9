package io.github.sspanak.tt9.ime;

import android.view.KeyEvent;

import io.github.sspanak.tt9.commands.CmdEditAdjacentLetter;
import io.github.sspanak.tt9.commands.CmdEditDuplicateLetter;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.commands.CmdShift;
import io.github.sspanak.tt9.commands.CmdSpaceKorean;
import io.github.sspanak.tt9.commands.Command;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.ime.helpers.InputConnectionAsync;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.util.Ternary;
import io.github.sspanak.tt9.util.chars.Characters;

public abstract class HotkeyHandler extends CommandHandler {
	private final CmdEditDuplicateLetter duplicateLetter = new CmdEditDuplicateLetter();
	private final CmdEditAdjacentLetter editAdjacentLetter = new CmdEditAdjacentLetter();
	private final CmdShift shift = new CmdShift();
	private final CmdSpaceKorean spaceKorean = new CmdSpaceKorean();

	private boolean waitingForSpaceTrim = false;


	protected boolean isHoldHotkey(int keyCode) {
		return
			keyCode < 0
			&& (
				Key.isHotkey(settings, keyCode)
				|| (Key.isArrowLeft(-keyCode) && InputModeKind.isRecomposing(mInputMode))
				|| (Key.isArrowRight(-keyCode) && InputModeKind.isRecomposing(mInputMode))
			);
	}


	@Override
	protected void onInit() {
		super.onInit();
		if (settings.areHotkeysInitialized()) {
			settings.setDefaultKeys();
		}
	}


	@Override
	public Ternary onBack() {
		waitingForSpaceTrim = false;
		if (super.onBack() == Ternary.TRUE) {
			return Ternary.TRUE;
		} else if (settings.isMainLayoutLarge()) {
			return Ternary.ALTERNATIVE;
		} else {
			return Ternary.FALSE;
		}
	}


	@Override
	protected boolean onNumber(int key, boolean hold, int repeat) {
		stopWaitingForSpaceTrimKey();

		if (hold && onHotkey(-Key.numberToCode(settings, key), false, false)) {
			return true;
		}

		return super.onNumber(key, hold, repeat);
	}


	@Override public boolean onOK() {
		return onOK(KeyEvent.KEYCODE_ENTER);
	}


	protected boolean onOK(int fromKey) {
		suggestionOps.cancelDelayedAccept();
		stopWaitingForSpaceTrimKey();

		if (suggestionOps.containsOnlyGuesses()) {
			onAcceptSuggestionManually(suggestionOps.acceptCurrent(), fromKey);
			return true;
		} else if (!suggestionOps.isEmpty()) {
			if (mInputMode.shouldReplacePreviousSuggestion(suggestionOps.getCurrent())) {
				mInputMode.onReplaceSuggestion(suggestionOps.getCurrentRaw());
			} else if (InputModeKind.isRecomposing(mInputMode)) {
				onAcceptSuggestionManually(suggestionOps.acceptEdited(), fromKey);
			} else {
				onAcceptSuggestionManually(suggestionOps.acceptCurrent(), fromKey);
			}

			return true;
		}

		int action = appHacks.getEditorAction(settings);

		boolean actionPerformed;

		if (action == TextField.IME_ACTION_ENTER) {
			actionPerformed = appHacks.onEnter();
			if (actionPerformed) {
				forceShowWindow();
			}

			// we don't want to use cached beforeCursor, because the action might have changed it
			updateShiftState(null, true, false);
			return actionPerformed;
		}

		actionPerformed = appHacks.onAction(action) || textField.performAction(action);
		if (!actionPerformed) {
			updateShiftState(null, true, false);
		}

		return actionPerformed;
	}


	public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly) {
		if (super.onHotkey(keyCode, repeat, validateOnly)) {
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_UNKNOWN || (keyCode < 0 && Key.isNumber(-keyCode) && !settings.getHoldToType())) {
			return false;
		}

		return
			onHardcodedKey(keyCode, validateOnly)
			|| onDynamicKey(keyCode, repeat, validateOnly);
	}


	private boolean onHardcodedKey(int keyCode, boolean validateOnly) {
		if (!validateOnly && Key.isArrow(keyCode)) {
			mindReader.clearContext();
		}

		if (Key.isArrowUp(keyCode) && duplicateLetter.runFromHotkey(getFinalContext(), validateOnly)) {
			return true;
		}

		if (Key.isArrowLeft(-keyCode) || Key.isArrowRight(-keyCode)) {
			if (editAdjacentLetter.runFromHotkey(getFinalContext(), validateOnly, Key.isArrowLeft(-keyCode))) {
				return true;
			}
		}

		return Key.isArrowLeft(keyCode) && onTrimTrailingSpace(validateOnly);
	}


	private boolean onDynamicKey(int keyCode, boolean repeat, boolean validateOnly) {
		if (keyCode == settings.getKeyShift()) {
			return
				shift.runFromHotkey(getFinalContext(), validateOnly)
				// when "Shift" and "Korean Space" share the same key, allow typing a space, when there
				// are no special characters to shift
				|| (keyCode == settings.getKeySpaceKorean() && spaceKorean.runFromHotkey(getFinalContext(), validateOnly));
		}

		final Command cmd = CommandCollection.getByHotkey(settings, keyCode);
		if (cmd instanceof CmdFilterSuggestions) {
			return ((CmdFilterSuggestions) cmd).runFromHotkey(getFinalContext(), validateOnly, repeat);
		} else {
			return cmd.runFromHotkey(getFinalContext(), validateOnly);
		}
	}


	@Override
	protected void waitForSpaceTrimKey() {
		waitingForSpaceTrim = true;
	}


	@Override
	protected void stopWaitingForSpaceTrimKey() {
		waitingForSpaceTrim = false;
	}


	public boolean onTrimTrailingSpace(boolean validateOnly) {
		if (!waitingForSpaceTrim || !settings.getAutoTrimTrailingSpace() || !suggestionOps.isEmpty()) {
			return false;
		}

		String after = textField.getStringAfterCursor(1);
		if (!after.isEmpty() && after.charAt(0) != '\n') {
			stopWaitingForSpaceTrimKey();
			return false;
		}

		String before = textField.getStringBeforeCursor(2);
		if (before.equals(InputConnectionAsync.TIMEOUT_SENTINEL) || before.length() != 2 || Character.isWhitespace(before.charAt(0)) || before.charAt(1) != Characters.getSpace(mLanguage).charAt(0)) {
			stopWaitingForSpaceTrimKey();
			return false;
		}

		if (!validateOnly) {
			textField.deleteChars(mLanguage, 1);
			stopWaitingForSpaceTrimKey();
		}

		return true;
	}
}
