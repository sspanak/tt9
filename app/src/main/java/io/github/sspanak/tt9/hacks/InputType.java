package io.github.sspanak.tt9.hacks;

import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import io.github.sspanak.tt9.ime.helpers.StandardInputType;

public class InputType extends StandardInputType {
	public InputType(InputConnection inputConnection, EditorInfo inputField){
		super(inputConnection, inputField);
	}


	boolean isGoogleChat() {
		return isAppField(
			"com.google.android.apps.dynamite",
			EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT
		);
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
				field.imeOptions == titleImeOptions
				|| field.imeOptions == titleAlternativeImeOptions
				|| field.imeOptions == authorImeOptions
			);
	}


	public boolean isLgX100SDialer() {
		int imeOptions = EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_ENTER_ACTION | EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS;
		return
			DeviceInfo.isLgX100S()
			&& isAppField("com.android.contacts", EditorInfo.TYPE_CLASS_PHONE)
			&& field.imeOptions == imeOptions;
	}


	boolean isMessenger() {
		return isAppField(
			"com.facebook.orca",
			EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
		);
	}


	/**
	 * Third-party apps are usually designed for a touch screen, so the least we can do is convert
	 * DPAD_CENTER to ENTER for typing new lines, regardless of the implementation of the OK key.
	 */
	boolean isMultilineTextInNonSystemApp() {
		return field != null && !field.packageName.contains("android") && isMultilineText();
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
			field != null && (field.packageName.startsWith("com.android") || field.packageName.startsWith("com.sonim"))
			&& (field.imeOptions & EditorInfo.IME_MASK_ACTION) == action
			&& (
				isText()
				// in some apps, they forgot to set the TEXT type, but fortunately, they did set the NO_SUGGESTIONS flag.
				|| ((field.inputType & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
			);
	}


	/**
	 * isTermux
	 * Termux is a terminal emulator and it naturally has a text input, but it incorrectly introduces itself as having a NULL input,
	 * instead of a plain text input. However NULL inputs are usually, buttons and dropdown menus, which indeed can not read text
	 * and are ignored by TT9 by default. In order not to ignore Termux, we need this.
	 */
	public boolean isTermux() {
		return isAppField("com.termux", EditorInfo.TYPE_NULL) && field.fieldId > 0;
	}

	public boolean isNotUs(Context context) {
		return !isAppField(context.getPackageName(), EditorInfo.TYPE_NULL);
	}


	/**
	 * isSpecialNumeric
	 * Calculator and Dialer fields seem to take care of numbers and backspace on their own,
	 * so we need to be aware of them.
	 * <p>
	 * NOTE: A Dialer field is not the same as Phone field. Dialer is where you
	 * actually dial and call a phone number. While the Phone field is a text
	 * field in any app or a webpage, intended for typing phone numbers.
	 * <p>
	 * More info (chronological order of bug fixing):
	 * <a href="https://github.com/sspanak/tt9/issues/46">the initial GitHub issue about Qin F21 Pro+</a>
	 * <a href="https://github.com/sspanak/tt9/pull/326">the PR about calculators</a>
	 * <a href="https://github.com/sspanak/tt9/issues/300">Dialer not detected correctly on LG X100S</a>
	 */
	protected boolean isSpecialNumeric() {
		return
			field.packageName.contains("com.android.calculator") // there is "calculator2", hence the contains()
			|| field.packageName.equals("com.android.dialer")
			|| isLgX100SDialer();
	}


	/**
	 * isAppField
	 * Detects a particular input field of a particular application.
	 */
	boolean isAppField(String appPackageName, int fieldSpec) {
		return
			field != null
			&& ((field.inputType & fieldSpec) == fieldSpec)
			&& field.packageName.equals(appPackageName);
	}
}
