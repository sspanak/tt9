package io.github.sspanak.tt9.ime.helpers;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.DeviceInfo;

public class AppHacks {
	private final EditorInfo editorInfo;
	private final InputConnection inputConnection;
	private final SettingsStore settings;
	private final TextField textField;
	private final InputType inputType;


	public AppHacks(SettingsStore settings, InputConnection inputConnection, EditorInfo inputField, TextField textField) {
		this.editorInfo = inputField;
		this.inputConnection = inputConnection;
		this.settings = settings;
		this.textField = textField;
		this.inputType = new InputType(inputConnection, inputField);
	}


	/**
	 * isKindleInvertedTextField
	 * When sharing a document to the Amazon Kindle app. It displays a screen where one could edit the title and the author of the
	 * document. These two fields do not support SpannableString, which is used for suggestion highlighting. When they receive one
	 * weird side effects occur. Nevertheless, all other text fields in the app are fine, so we detect only these two particular ones.
	 */
	private boolean isKindleInvertedTextField() {
		int titleImeOptions = EditorInfo.IME_ACTION_NONE | EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NAVIGATE_NEXT;
		int titleAlternativeImeOptions = titleImeOptions | EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS; // sometimes the title field is different for no reason
		int authorImeOptions = EditorInfo.IME_ACTION_SEND | EditorInfo.IME_ACTION_GO | EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS;

		return
			isAppField("com.amazon.kindle", EditorInfo.TYPE_CLASS_TEXT)
			&& (
				editorInfo.imeOptions == titleImeOptions
				|| editorInfo.imeOptions == titleAlternativeImeOptions
				|| editorInfo.imeOptions == authorImeOptions
			);
	}


	/**
	 * isTermux
	 * Termux is a terminal emulator and it naturally has a text input, but it incorrectly introduces itself as having a NULL input,
	 * instead of a plain text input. However NULL inputs are usually, buttons and dropdown menus, which indeed can not read text
	 * and are ignored by TT9 by default. In order not to ignore Termux, we need this.
	 */
	public boolean isTermux() {
		return isAppField("com.termux", EditorInfo.TYPE_NULL) && editorInfo.fieldId > 0;
	}


	/**
	 * isMessenger
	 * Facebook Messenger has flaky support for sending messages. To fix that, we detect the chat input field and send the appropriate
	 * key codes to it. See "onFbMessengerEnter()" for info how the hack works.
	 */
	private boolean isMessenger() {
		return isAppField(
			"com.facebook.orca",
			EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
		);
	}


	private boolean isMultilineTextInNonSystemApp() {
		return editorInfo != null && !editorInfo.packageName.contains("android") && inputType.isMultilineText();
	}


	private boolean isGoogleChat() {
		return isAppField(
			"com.google.android.apps.dynamite",
			EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT
		);
	}


	/**
	 * Simulate the behavior of the Sonim native keyboard. In search fields with integrated lists,
	 * ENTER is used to select an item from the list. But some of them have actionId = NEXT, instead of NONE,
	 * which normally means "navigate to the next button or field". This hack correctly allows selection
	 * of the item, instead of performing navigation.
	 */
	private boolean isSonimSearchField(int action) {
		return
			DeviceInfo.isSonim() &&
			editorInfo != null && (editorInfo.packageName.startsWith("com.android") || editorInfo.packageName.startsWith("com.sonim"))
			&& (editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION) == action
			&& (
				inputType.isText()
				// in some apps, they forgot to set the TEXT type, but fortunately, they did set the NO_SUGGESTIONS flag.
				|| ((editorInfo.inputType & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
			);
	}


	/**
	 * isAppField
	 * Detects a particular input field of a particular application.
	 */
	private boolean isAppField(String appPackageName, int fieldSpec) {
		return
			editorInfo != null
			&& ((editorInfo.inputType & fieldSpec) == fieldSpec)
			&& editorInfo.packageName.equals(appPackageName);
	}


	/**
	 * setComposingTextWithHighlightedStem
	 * A compatibility function for text fields that do not support SpannableString. Effectively disables highlighting.
	 */
	public void setComposingTextWithHighlightedStem(@NonNull String word, InputMode inputMode) {
		if (isKindleInvertedTextField()) {
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
	public boolean onBackspace(InputMode inputMode) {
		if (isKindleInvertedTextField()) {
			inputMode.clearWordStem();
		} else if (isTermux()) {
			return false;
		}

		// When there is no text, allow double function keys to function normally (e.g. "Back" navigates back)
		return inputMode.getSuggestions().isEmpty() && textField.getStringBeforeCursor(1).isEmpty();
	}


	/**
	 * onAction
	 * Runs non-standard actions for certain apps and fields. Use instead of inputConnection.performEditorAction(action).
	 * Returns "true" if the action was handled, "false" otherwise.
	 */
	public boolean onAction(int action) {
		if (isSonimSearchField(action)) {
			return sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
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
		if (settings.getFbMessengerHack() && isMessenger()) {
			return onEnterFbMessenger();
		} else if (settings.getGoogleChatHack() && isGoogleChat()) {
			return onEnterGoogleChat();
		} else if (isTermux() || isMultilineTextInNonSystemApp()) {
			// Termux supports only ENTER, so we convert DPAD_CENTER for it.
			// Any extra installed apps are likely not designed for hardware keypads, so again,
			// we don't want to send DPAD_CENTER to them.
			return sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
		}

		// The rest of the cases are probably system apps or numeric fields, which should
		// now how to handle the incoming OK key code, be it ENTER or DPAD_CENTER.
		// As per the docs, we must return "false", to indicate that we have not "seen" the key press.
		return false;
	}

	/**
	 * onEnterFbMessenger
	 * Messenger responds only to ENTER, but not DPAD_CENTER, so we make sure to send the correct code,
	 * no matter how the hardware key is implemented.
	 */
	private boolean onEnterFbMessenger() {
		if (inputConnection == null || textField == null || !textField.isThereText()) {
			return false;
		}

		sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);

		return true;
	}

	/**
	 * onEnterGoogleChat
	 * Google Chat does not seem to respond consistently to ENTER. So we trick it by selecting
	 * the send button it, then going back to the text field, so that one can continue typing.
	 */
	private boolean onEnterGoogleChat() {
		if (inputConnection == null || textField == null || !textField.isThereText()) {
			return false;
		}

		sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB);
		sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB);
		sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
		sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB, true);
		sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB, true);
		sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB, true);
		sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB, true);

		return true;
	}


	private boolean sendDownUpKeyEvents(int keyCode) {
		return sendDownUpKeyEvents(keyCode, false);
	}


	private boolean sendDownUpKeyEvents(int keyCode, boolean shift) {
		if (inputConnection != null) {
			KeyEvent downEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, shift ? KeyEvent.META_SHIFT_ON : 0);
			KeyEvent upEvent = new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, shift ? KeyEvent.META_SHIFT_ON : 0);
			return inputConnection.sendKeyEvent(downEvent) && inputConnection.sendKeyEvent(upEvent);
		}

		return false;
	}
}
