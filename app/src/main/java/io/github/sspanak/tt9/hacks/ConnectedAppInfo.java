package io.github.sspanak.tt9.hacks;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import io.github.sspanak.tt9.ime.helpers.InputType;

public class ConnectedAppInfo {
	final EditorInfo editorInfo;
	final InputType inputType;


	ConnectedAppInfo(InputConnection inputConnection, EditorInfo inputField) {
		this.editorInfo = inputField;
		this.inputType = new InputType(inputConnection, inputField);
	}


		/**
	 * isKindleInvertedTextField
	 * When sharing a document to the Amazon Kindle app. It displays a screen where one could edit the title and the author of the
	 * document. These two fields do not support SpannableString, which is used for suggestion highlighting. When they receive one
	 * weird side effects occur. Nevertheless, all other text fields in the app are fine, so we detect only these two particular ones.
	 */
	boolean isKindleInvertedTextField() {
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
	boolean isMessenger() {
		return isAppField(
			"com.facebook.orca",
			EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
		);
	}


	boolean isMultilineTextInNonSystemApp() {
		return editorInfo != null && !editorInfo.packageName.contains("android") && inputType.isMultilineText();
	}


	boolean isGoogleChat() {
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
	boolean isSonimSearchField(int action) {
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
	boolean isAppField(String appPackageName, int fieldSpec) {
		return
			editorInfo != null
			&& ((editorInfo.inputType & fieldSpec) == fieldSpec)
			&& editorInfo.packageName.equals(appPackageName);
	}
}
