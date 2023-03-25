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
	 *
	 * https://developer.android.com/reference/android/text/InputType#TYPE_NULL
	 */
	public boolean isLimited() {
		return field != null && field.inputType == android.text.InputType.TYPE_NULL;
	}


	/**
	 * isDialer
	 * Dialer fields seem to take care of numbers and backspace on their own,
	 * so we need to be aware of them.
	 *
	 * NOTE: A Dialer field is not the same as a Phone field in a phone book.
	 */
	public boolean isDialer() {
		if (field == null) {
			return false;
		}

		int inputType = field.inputType & android.text.InputType.TYPE_MASK_CLASS;

		return
			inputType == android.text.InputType.TYPE_CLASS_PHONE && field.packageName.equals("com.android.dialer");
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


	/**
	 * isFilter
	 * handle filter list cases... do not hijack DPAD center and make sure back's go through proper
	 */
	public boolean isFilter() {
		if (field == null) {
			return false;
		}

		int inputType = field.inputType & android.text.InputType.TYPE_MASK_CLASS;
		int inputVariation = field.inputType & android.text.InputType.TYPE_MASK_VARIATION;

		return inputType == android.text.InputType.TYPE_CLASS_TEXT && inputVariation == android.text.InputType.TYPE_TEXT_VARIATION_FILTER;
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
