package io.github.sspanak.tt9.ime.helpers;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class AppHacks {
	private final EditorInfo editorInfo;
	private final InputConnection inputConnection;
	private final SettingsStore settings;
	private final TextField textField;


	public AppHacks(SettingsStore settings, InputConnection inputConnection, EditorInfo inputField, TextField textField) {
		this.editorInfo = inputField;
		this.inputConnection = inputConnection;
		this.settings = settings;
		this.textField = textField;
	}


	/**
	 * isKindleInvertedTextField
	 * When sharing a document to the Amazon Kindle app. It displays a screen where one could edit the title and the author of the
	 * document. These two fields do not support SpannableString, which is used for suggestion highlighting. When they receive one
	 * weird side effects occur. Nevertheless, all other text fields in the app are fine, so we detect only these two particular ones.
	 */
	private boolean isKindleInvertedTextField() {
		return isAppField("com.amazon.kindle", EditorInfo.TYPE_CLASS_TEXT);
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
	public boolean setComposingTextWithHighlightedStem(@NonNull String word) {
		if (isKindleInvertedTextField()) {
			textField.setComposingText(word);
			return true;
		}

		return false;
	}


	/**
	 * onBackspace
	 * Performs extra Backspace operations and returns "false", or completely replaces Backspace and returns "true". When "true" is
	 * returned, you must not attempt to delete text. This function has already done everything necessary.
	 */
	public boolean onBackspace(InputMode inputMode) {
		if (isKindleInvertedTextField()) {
			inputMode.clearWordStem();
			return true;
		} else if (isTermux()) {
			return settings.getKeyBackspace() != KeyEvent.KEYCODE_BACK;
		}

		return false;
	}


	/**
	 * onEnter
	 * Tries to guess and send the correct confirmation key code or sequence of key codes, depending on the connected application
	 * and input field. On invalid connection or field, it does nothing.
	 * This hack applies to all applications, not only selected ones.
	 */
	public boolean onEnter() {
		if (isTermux()) {
			sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			return true;
		} else if (isMessenger()) {
			return onEnterFbMessenger();
		}

		return onEnterDefault();
	}


	/**
	 * onEnterDefault
	 * This is the default "ENTER" routine for most applications that support send-with-enter functionality. It will attempt to
	 * guess and send the correct confirmation key code, be it "ENTER" or "DPAD_CENTER".
	 * On invalid textField, it does nothing.
	 */
	private boolean onEnterDefault() {
		if (textField == null) {
			return false;
		}

		String oldText = textField.getTextBeforeCursor() + textField.getTextAfterCursor();

		sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_CENTER);

		// If there is no text, there is nothing to send, so there is no need to attempt any hacks.
		// We just pass through DPAD_CENTER and finish as if the key press was handled by the system.
		if (oldText.isEmpty()) {
			return true;
		}

		try {
			// In Android there is no strictly defined confirmation key, hence DPAD_CENTER may have done nothing.
			// If so, send an alternative key code as a final resort.
			Thread.sleep(80);
			String newText = textField.getTextBeforeCursor() + textField.getTextAfterCursor();
			if (newText.equals(oldText)) {
				sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			}
		} catch (InterruptedException e) {
			// This thread got interrupted. Assume it's because the connected application has taken an action
			// after receiving DPAD_CENTER, so we don't need to do anything else.
			return true;
		}

		return true;
	}


	/**
	 * onEnterFbMessenger
	 * Once we have detected the chat message field we apply the appropriate key combo to send the message.
	 */
	private boolean onEnterFbMessenger() {
		if (textField == null) {
			return false;
		}

		// in case the setting is disabled, just type a new line as one would expect
		if (!settings.getFbMessengerHack()) {
			inputConnection.commitText("\n", 1);
			return true;
		}

		// do not send any commands if the user has not typed anything or the field is invalid
		if (!textField.isThereText()) {
			return false;
		}

		if (isMessenger()) {
			// Messenger responds only to ENTER, but not DPAD_CENTER, so we make sure to send the correct code,
			// no matter how the hardware key is implemented.
			sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
		}

		return true;
	}


	private void sendDownUpKeyEvents(int keyCode) {
		if (inputConnection != null) {
			inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
			inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
		}
	}
}
