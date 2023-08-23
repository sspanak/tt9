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
		return editorInfo != null && editorInfo.inputType == 1 && editorInfo.packageName.contains("com.amazon.kindle");
	}


	/**
	 * isTermux
	 * Termux is a terminal emulator and it naturally has a text input, but it incorrectly introduces itself as having a NULL input,
	 * instead of a plain text input. However NULL inputs are usually, buttons and dropdown menus, which indeed can not read text
	 * and are ignored by TT9 by default. In order not to ignore Termux, we need this.
	 */
	public boolean isTermux() {
		return editorInfo != null && editorInfo.inputType == 0 && editorInfo.fieldId > 0 && editorInfo.packageName.contains("com.termux");
	}


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


	private void sendDownUpKeyEvents(int keyCode) {
		if (inputConnection != null) {
			inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
			inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
		}
	}
}
