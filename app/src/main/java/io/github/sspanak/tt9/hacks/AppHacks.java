package io.github.sspanak.tt9.hacks;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.helpers.CursorOps;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.ime.helpers.SuggestionOps;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.helpers.TextSelection;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Timer;

public class AppHacks {
	private final static String TYPING_SESSION_TIMER = "TYPING_SESSION_TIMER";
	private static boolean previousWasMessengerChat = false;


	private final InputType inputType;
	private final TextField textField;
	private final TextSelection textSelection;


	public AppHacks(InputType inputType, TextField textField, TextSelection textSelection) {
		this.inputType = inputType;
		this.textField = textField;
		this.textSelection = textSelection;
	}


	/**
	 * setComposingTextWithHighlightedStem
	 * A compatibility function for text fields that do not support SpannableString. Effectively disables highlighting.
	 */
	public void setComposingTextWithHighlightedStem(@NonNull String word, InputMode inputMode) {
		if (inputType.isKindleInvertedTextField()) {
			textField.setComposingText(word);
		} else {
			textField.setComposingTextWithHighlightedStem(word, inputMode);
		}
	}


	/**
	 * onBackspace
	 * Performs extra Backspace operations and returns "false", or completely replaces Backspace and returns "true". When "true" is
	 * returned, you must not attempt to delete text. This function has already done everything necessary.
	 */
	public boolean onBackspace(@NonNull SettingsStore settings, @NonNull InputMode inputMode) {
		if (inputType.isKindleInvertedTextField()) {
			inputMode.clearWordStem();
		} else if (inputType.isTermux()) {
			return false;
		}

		// When Backspace function is assigned to a different key (not hardware Backspace), we must
		// allow the key to function normally if there is nothing to delete (e.g. "Back" navigates back).
		return
			Key.exists(settings.getKeyBackspace())
			&& !Key.isHardwareBackspace(settings.getKeyBackspace())
			&& inputMode.getSuggestions().isEmpty()
			&& textSelection.isEmpty()
			&& textField.getStringBeforeCursor(1).isEmpty();
	}


	/**
	 * onAction
	 * Runs non-standard actions for certain apps and fields. Use instead of inputConnection.performEditorAction(action).
	 * Returns "true" if the action was handled, "false" otherwise.
	 */
	public boolean onAction(int action) {
		if (inputType.isSonimSearchField(action)) {
			return textField.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
		}

		return false;
	}


	/**
	 * Handles applications that always report no text around the cursor, preventing the cursor from
	 * moving the usual way.
	 */
	public boolean onMoveCursor(boolean backward) {
		if (inputType.isRustDesk() || inputType.isTermux()) {
			return textField.sendDownUpKeyEvents(backward ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
		}

		return false;
	}


	/**
	 * Performs extra operations when the cursor moves and returns "true" if the selection was handled,
	 * "false" otherwise.
	 * CURSOR RESET
	 * When sending messages using the Viber or the SMS app SEND button, it does so and clears the text
	 * field, but without notifying the keyboard. This means, after sending the message, the InputMode
	 * still holds the old text, while the text field is empty. Attempting to type a new word then
	 * results in appending to the old word. We use this hack to detect Viber and reset the InputMode
	 * upon sending a message.
	 */
	public boolean onUpdateSelection(
		@NonNull InputMode inputMode,
		@NonNull SuggestionOps suggestionOps,
		int oldSelStart,
		int oldSelEnd,
		int newSelStart,
		int newSelEnd,
		int candidatesStart,
		int candidatesEnd
	) {
		if (CursorOps.isInputReset(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd) && textField.isEmpty()) {
			inputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
			inputMode.reset();
			return true;
		}

		return false;
	}


	/**
	 * onEnter
	 * Tries to guess and send the correct confirmation key code or sequence of key codes,
	 * depending on the connected application and input field. On invalid connection or field,
	 * it does nothing and return "false", signaling the system we have ignored the key press.
	 */
	public boolean onEnter() {
		if (inputType.isTermux() || inputType.isMultilineTextInNonSystemApp()) {
			// Termux supports only ENTER, so we convert DPAD_CENTER for it.
			// Any extra installed apps are likely not designed for hardware keypads, so again,
			// we don't want to send DPAD_CENTER to them.
			return textField.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
		}

		// The rest of the cases are probably system apps or numeric fields, which should
		// now how to handle the incoming OK key code, be it ENTER or DPAD_CENTER.
		// As per the docs, we must return "false", to indicate that we have not "seen" the key press.
		return false;
	}


	public static void onStart(@NonNull SettingsStore settings, @NonNull EditorInfo field) {
		// currently, onStart() only adjusts the padding of MainSmall, so save some resources by not
		// doing anything if another layout is used.
		if (!settings.isMainLayoutSmall()) {
			settings.setMessengerReplyExtraPadding(false);
			return;
		}

		final InputType newInputType = new InputType(null, field);
		if (newInputType.notMessenger()) {
			settings.setMessengerReplyExtraPadding(false);
			return;
		}

		final long previousSessionTime = Timer.stop(TYPING_SESSION_TIMER);
		final boolean currentIsMessengerNonText = newInputType.isMessengerNonText();

		if (previousSessionTime < 1000 && previousWasMessengerChat && currentIsMessengerNonText) {
			settings.setMessengerReplyExtraPadding(true);
		} else if (previousSessionTime > 1000 && previousWasMessengerChat) {
			settings.setMessengerReplyExtraPadding(false);
		}

		Timer.start(TYPING_SESSION_TIMER);
		previousWasMessengerChat = newInputType.isMessengerChat();
	}
}
