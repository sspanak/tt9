package io.github.sspanak.tt9.ime.helpers;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import io.github.sspanak.tt9.ime.modes.InputMode;


abstract public class StandardInputType {
	private static final int TYPE_MULTILINE_TEXT = EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;

	@Nullable protected final InputMethodService ims;
	protected final EditorInfo field;


	protected StandardInputType(@Nullable InputMethodService ims, EditorInfo inputField) {
		this.ims = ims;
		field = inputField;
	}


	@Nullable
	protected InputConnection getConnection() {
		return ims != null ? ims.getCurrentInputConnection() : null;
	}


	public boolean isValid() {
		return field != null && getConnection() != null;
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
		if (!isText()) {
			return false;
		}

		int variation = field.inputType & InputType.TYPE_MASK_VARIATION;

		return
			variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
			|| variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
	}


	public boolean isPassword() {
		if (!isText()) {
			return false;
		}

		int variation = field.inputType & InputType.TYPE_MASK_VARIATION;

		return
			variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
			|| variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
			|| variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
	}


	boolean isPersonName() {
		return isText() && (field.inputType & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
	}


	public boolean isSpecialized() {
		return isEmail() || isPassword() || isUri();
	}


	public boolean isText() {
		return field != null && (field.inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT;
	}


	abstract public boolean isDefectiveText();

	private boolean isNoSuggestionsText() {
		return isText() && (field.inputType & InputType.TYPE_MASK_FLAGS & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
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
	 * @return Set<InputMode.MODE_PASSTHROUGH | InputMode.MODE_ABC | InputMode.MODE_123 | InputMode.MODE_PREDICTIVE>
	 */
	public Set<Integer> determineInputModes(@NonNull Context context) {
		Set<Integer> allowedModes = new HashSet<>();

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
				// Enable predictions for incorrectly defined text fields.
				if (isDefectiveText()) {
					allowedModes.add(InputMode.MODE_PREDICTIVE);
				}

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
		if (getConnection() == null || field == null || field.inputType == InputType.TYPE_NULL) {
			return InputMode.CASE_UNDEFINED;
		}

		if (isPersonName()) {
			return InputMode.CASE_CAPITALIZE;
		}

		if (isSpecialized() || isNoSuggestionsText()) {
			return InputMode.CASE_LOWER;
		}

		return switch (field.inputType & InputType.TYPE_MASK_FLAGS) {
			case InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS -> InputMode.CASE_UPPER;
			case InputType.TYPE_TEXT_FLAG_CAP_WORDS -> InputMode.CASE_CAPITALIZE;
			default -> InputMode.CASE_UNDEFINED;
		};

	}
}
