package io.github.sspanak.tt9.hacks;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.helpers.StandardInputType;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class InputType extends StandardInputType {
	private final boolean isUs;

	public InputType(@Nullable InputMethodService ims, EditorInfo inputField) {
		super(ims, inputField);
		isUs = isAppField(ims != null ? ims.getPackageName() : null, EditorInfo.TYPE_NULL);
	}


	@Override
	public boolean isEmail() {
		return super.isEmail() && !isAndroidContactsSearch();
	}


	/**
	 * isAndroid15ContactsField
	 * "First Name" and "Last Name" fields in Android 15 are specified absolutely incorrectly.
	 * Thank you for wasting my time, Google!
	 */
	private boolean isAndroid15ContactsField() {
		return
			isAppInput("com.google.android.contacts", 8288)
			&& field.privateImeOptions != null && field.privateImeOptions.contains("requestPhoneticOutput");
	}


	/**
	 * Yet another randomly defined field. On Unihertz Atom L (Android 11) and Show F2 (Android 8),
	 * the search field in the Contacts app is defined as "email", instead of a plain text field, like
	 * in other devices. This causes us to display suggestions for email addresses, which is not desired.
	 * Bug reports:
	 * <a href="https://github.com/sspanak/tt9/issues/698">#698</a>
	 * <a href="https://github.com/sspanak/tt9/issues/735">#735</a>
	 */
	public boolean isAndroidContactsSearch() {
		if (field == null) {
			return false;
		}

		return
			(isAppInput("com.android.contacts", 33)) // only detect the old Contacts
			&& (field.imeOptions & EditorInfo.IME_FLAG_NO_EXTRACT_UI) == EditorInfo.IME_FLAG_NO_EXTRACT_UI;
	}


	public boolean isCalculator() {
		return field != null
			&& (field.packageName.endsWith("calculator") || field.packageName.endsWith(".calc"))
			&& (field.inputType & EditorInfo.TYPE_MASK_CLASS) == EditorInfo.TYPE_CLASS_NUMBER;
	}


	/**
	 * isDuoLingoReportBug
	 * When reporting a bug in the Duolingo app, the text field is missing the TYPE_TEXT flag, which
	 * causes us to detect it as a numeric field. This effectively disables Predictive mode, which is
	 * actually desired there. Here, we detect this particular case and treat it as a text field.
	 */
	private boolean isDuoLingoReportBug() {
		return
			field != null
			&& "com.duolingo".equals(field.packageName)
			&& field.inputType == EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
			&& field.imeOptions == EditorInfo.IME_ACTION_DONE;
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


	public boolean isDumbPhoneDialer(Context context) {
		// the inputType is not always TYPE_CLASS_PHONE on all devices, so we must not filter by that.
		return field.packageName.endsWith(".dialer") && !DeviceInfo.noKeyboard(context) && !isText();
	}


	public boolean isLgX100SDialer() {
		int imeOptions = EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_ENTER_ACTION;
		return
			DeviceInfo.IS_LG_X100S
			&& isAppField("com.android.contacts", EditorInfo.TYPE_CLASS_PHONE)
			&& ((field.imeOptions & imeOptions) == imeOptions);
	}


	public boolean notMessenger() {
		return field == null || !field.packageName.equals("com.facebook.orca");
	}

	public boolean isMessengerChat() {
		return isAppInput("com.facebook.orca", 147457);
	}


	public boolean isMessengerNonText() {
		return isAppInput("com.facebook.orca", EditorInfo.TYPE_NULL);
	}


	/**
	 * Third-party apps are usually designed for a touch screen, so the least we can do is convert
	 * DPAD_CENTER to ENTER for typing new lines, regardless of the implementation of the OK key.
	 */
	boolean isMultilineTextInNonSystemApp() {
		return field != null && !field.packageName.contains("android") && isMultilineText();
	}


	/**
	 * RustDesk does not support composing text when connected to a remote computer. This detects the
	 * "remote input field", so that we can prevent inserting any composing text, but still perform
	 * the composing operation behind the scenes.
	 */
	public boolean isRustDesk() {
		final int OPTIONS_MASK = EditorInfo.IME_ACTION_NONE | EditorInfo.IME_FLAG_NO_FULLSCREEN;

		return isAppField(
			"com.carriez.flutter_hbb",
			EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
		) && (field.imeOptions & OPTIONS_MASK) == OPTIONS_MASK;
	}


	/**
	 * Simulate the behavior of the Sonim native keyboard. In search fields with integrated lists,
	 * ENTER is used to select an item from the list. But some of them have actionId = NEXT, instead of NONE,
	 * which normally means "navigate to the next button or field". This hack correctly allows selection
	 * of the item, instead of performing navigation.
	 */
	boolean isSonimSearchField(int action) {
		return
			DeviceInfo.IS_SONIM &&
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


	/**
	 * isTeams
	 * M$ Teams seems to control the keyboard visibility on its own. Initially, it always reports
	 * the input fields as TYPE_NULL, but once the keyboard accepts the show request, it switches to
	 * TYPE_CLASS_TEXT. This method used to enforce us to stay active at all times in Teams.
	 * The problem does not occur on all Android versions. I was able to reproduce it only on Unihertz
	 * Atom L (Android 11), but not on Energizer H620S (Android 10). The bug report also suggests it
	 * occurs on newer versions. See: <a href="https://github.com/sspanak/tt9/issues/749">#749</a>.
	 */
	public boolean isTeams() {
		return isAppField("com.microsoft.teams", EditorInfo.TYPE_NULL);
	}


	public boolean isUs() {
		return isUs;
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
	 * <a href="https://github.com/sspanak/tt9/issues/216">LG Classic Flip says the dialer is a multi-line text field</a>
	 * <a href="https://github.com/sspanak/tt9/pull/326">the PR about calculators</a>
	 * <a href="https://github.com/sspanak/tt9/issues/300">Dialer not detected correctly on LG X100S</a>
	 * [NO ISSUE] On touchscreen-only phones, in the Dialer app, we mustn't switch to passthrough, because
	 * they don't have a physical keyboard.
	 * <a href="https://github.com/sspanak/tt9/issues/538">Beeps on CAT S22 Flip</a>
	 * <a href="https://github.com/sspanak/tt9/issues/549">The UI does not appear on Xiaomi Redmi 12c</a>
	 * <a href="https://github.com/sspanak/tt9/issues/827">UI not hiding in 3rd-party calculators</a>
	 */
	protected boolean isSpecialNumeric(Context context) {
		return isCalculator() || isDumbPhoneDialer(context) || isLgX100SDialer();
	}


	/**
	 * Detects incorrectly defined text fields.
	 */
	@Override
	public boolean isDefectiveText() {
		return isDuoLingoReportBug() || isAndroid15ContactsField();
	}


	/**
	 * isAppField
	 * Checks if a particular app field has specific inputType flags.
	 */
	boolean isAppField(String appPackageName, int fieldSpec) {
		return
			field != null
			&& ((field.inputType & fieldSpec) == fieldSpec)
			&& field.packageName.equals(appPackageName);
	}

	/**
	 * Similar to isAppField, but checks the inputType for strict equality, instead for contained flags.
	 */
	boolean isAppInput(String appPackageName, int inputType) {
		return
			field != null
			&& field.inputType == inputType
			&& field.packageName.equals(appPackageName);
	}
}
