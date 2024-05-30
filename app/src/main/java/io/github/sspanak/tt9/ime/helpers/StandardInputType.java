package io.github.sspanak.tt9.ime.helpers;

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.modes.InputMode;


abstract public class StandardInputType {
	private static final int TYPE_MULTILINE_TEXT = EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;

	protected final InputConnection connection;
	protected final EditorInfo field;


	protected StandardInputType(InputConnection inputConnection, EditorInfo inputField) {
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
		return field != null && field.inputType == InputType.TYPE_NULL;
	}


	public boolean isPhoneNumber() {
		return
			field != null
			&& (field.inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_PHONE;
	}

	public boolean isNumeric() {
		return
			field != null
			&& (field.inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER;
	}

	public boolean isDecimal() {
		return
			isNumeric()
			&& (field.inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) == InputType.TYPE_NUMBER_FLAG_DECIMAL;
	}


	public boolean isSignedNumber() {
		return
			isNumeric()
			&& (field.inputType & InputType.TYPE_NUMBER_FLAG_SIGNED) == InputType.TYPE_NUMBER_FLAG_SIGNED;
	}


	abstract protected boolean isSpecialNumeric(Context context);


	public boolean isEmail() {
		if (field == null) {
			return false;
		}

		int variation = field.inputType & InputType.TYPE_MASK_VARIATION;

		return
			variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
			|| variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
	}


	public boolean isPassword() {
		if (field == null) {
			return false;
		}

		int variation = field.inputType & InputType.TYPE_MASK_VARIATION;

		return
			variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
			|| variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
			|| variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
	}


	boolean isPersonName() {
		return field != null && (field.inputType & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
	}


	public boolean isSpecialized() {
		return isEmail() || isPassword() || isUri();
	}


	public boolean isText() {
		return field != null && (field.inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT;
	}


	public boolean isMultilineText() {
		return field != null && (field.inputType & TYPE_MULTILINE_TEXT) == TYPE_MULTILINE_TEXT;
	}


	private boolean isUri() {
		return field != null && (field.inputType & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_URI;
	}


	/**
	 * determineInputModes
	 * Determine the typing mode based on the input field being edited. Returns an ArrayList of the allowed modes.
	 *
	 * @return ArrayList<SettingsStore.MODE_ABC | SettingsStore.MODE_123 | SettingsStore.MODE_PREDICTIVE>
	 */
	public ArrayList<Integer> determineInputModes(Context context) {
		ArrayList<Integer> allowedModes = new ArrayList<>();

		if (field == null) {
			allowedModes.add(InputMode.MODE_PASSTHROUGH);
			return allowedModes;
		}

		// Calculators (only 0-9 and math) and Dialer (0-9, "#" and "*") fields
		// handle all input themselves, so we are supposed to pass through all key presses.
		// Note: A Dialer field is not a Phone number field.
		if (isSpecialNumeric(context)) {
			allowedModes.add(InputMode.MODE_PASSTHROUGH);
			return allowedModes;
		}

		switch (field.inputType & InputType.TYPE_MASK_CLASS) {
			case InputType.TYPE_CLASS_NUMBER:
			case InputType.TYPE_CLASS_DATETIME:
			case InputType.TYPE_CLASS_PHONE:
				// Numbers, dates and phone numbers default to the numeric keyboard,
				// with no extra features.
				allowedModes.add(InputMode.MODE_123);
				return allowedModes;

			case InputType.TYPE_CLASS_TEXT:
				// This is general text editing. We will default to the
				// normal alphabetic keyboard, and assume that we should
				// be doing predictive text (showing candidates as the
				// user types).
				if (!isPassword()) {
					allowedModes.add(InputMode.MODE_PREDICTIVE);
				}

				// ↓ fallthrough to add ABC and 123 modes ↓

			default:
				// For all unknown input types, default to the alphabetic
				// keyboard with no special features.
				allowedModes.add(InputMode.MODE_123);
				allowedModes.add(InputMode.MODE_ABC);

				return allowedModes;
		}
	}


	/**
	 * Helper to update the shift state of our keyboard based on the initial
	 * editor state.
	 */
	public int determineTextCase() {
		if (connection == null || field == null || field.inputType == InputType.TYPE_NULL) {
			return InputMode.CASE_UNDEFINED;
		}

		if (isSpecialized()) {
			return InputMode.CASE_LOWER;
		}

		if (isPersonName()) {
			return InputMode.CASE_CAPITALIZE;
		}

		switch (field.inputType & InputType.TYPE_MASK_FLAGS) {
			case InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS:
				return InputMode.CASE_UPPER;
			case InputType.TYPE_TEXT_FLAG_CAP_WORDS:
				return InputMode.CASE_CAPITALIZE;
		}

		return InputMode.CASE_UNDEFINED;
	}
}
