package io.github.sspanak.tt9.ime.helpers;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;


public class InputType {
	private final InputConnection connection;
	private final EditorInfo field;


	public InputType(InputConnection inputConnection, EditorInfo inputField) {
		connection = inputConnection;
		field = inputField;
	}


	public boolean isValid() {
		return field != null && connection != null;
	}


	/**
	 * isLimited
	 * Special or limited input type means the input connection is not rich,
	 * or it can not process or show things like candidate text, nor retrieve the current text.
	 * <p>
	 * More info: <a href="https://developer.android.com/reference/android/text/InputType#TYPE_NULL">android docs</a>.
	 */
	public boolean isLimited() {
		return field != null && field.inputType == android.text.InputType.TYPE_NULL;
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
	 * More info: <a href="https://github.com/sspanak/tt9/issues/46">in this Github issue</a>
	 * and <a href="https://github.com/sspanak/tt9/pull/326">the PR about calculators</a>.
	 */
	public boolean isSpecialNumeric() {
		return
			isPhoneNumber() && field.packageName.equals("com.android.dialer")
			|| isNumeric() && field.packageName.contains("com.android.calculator");
	}


	public boolean isPhoneNumber() {
		return
			field != null
			&& (field.inputType & android.text.InputType.TYPE_MASK_CLASS) == android.text.InputType.TYPE_CLASS_PHONE;
	}

	public boolean isNumeric() {
		return
			field != null
			&& (field.inputType & android.text.InputType.TYPE_MASK_CLASS) == android.text.InputType.TYPE_CLASS_NUMBER;
	}

	public boolean isDecimal() {
		return
			isNumeric()
			&& (field.inputType & android.text.InputType.TYPE_MASK_FLAGS) == android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;
	}


	public boolean isSignedNumber() {
		return
			isNumeric()
			&& (field.inputType & android.text.InputType.TYPE_MASK_FLAGS) == android.text.InputType.TYPE_NUMBER_FLAG_SIGNED;
	}


	public boolean isEmail() {
		if (field == null) {
			return false;
		}

		int variation = field.inputType & android.text.InputType.TYPE_MASK_VARIATION;

		return
			variation == android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
			|| variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
	}


	public boolean isPassword() {
		if (field == null) {
			return false;
		}

		int variation = field.inputType & android.text.InputType.TYPE_MASK_VARIATION;

		return
			variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
			|| variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
			|| variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
	}


	boolean isPersonName() {
		return field != null && (field.inputType & android.text.InputType.TYPE_MASK_VARIATION) == android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
	}


	public boolean isSpecialized() {
		return isEmail() || isPassword() || isUri();
	}


	private boolean isUri() {
		return field != null && (field.inputType & android.text.InputType.TYPE_MASK_VARIATION) == android.text.InputType.TYPE_TEXT_VARIATION_URI;
	}
}
