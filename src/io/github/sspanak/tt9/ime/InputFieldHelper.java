package io.github.sspanak.tt9.ime;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.ime.modes.InputMode;


class InputFieldHelper {
	public static boolean isThereText(InputConnection currentInputConnection) {
		if (currentInputConnection == null) {
			return false;
		}

		ExtractedText extractedText = currentInputConnection.getExtractedText(new ExtractedTextRequest(), 0);
		return extractedText != null && extractedText.text.length() > 0;
	}


	public static boolean isSpecializedTextField(EditorInfo inputField) {
		if (inputField == null) {
			return false;
		}

		int variation = inputField.inputType & InputType.TYPE_MASK_VARIATION;

		return (
				variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
				|| variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
				|| variation == InputType.TYPE_TEXT_VARIATION_FILTER
		);
	}


	/**
	 * isFilterTextField
	 * handle filter list cases... do not hijack DPAD center and make sure back's go through proper
	 */
	public static boolean isFilterTextField(EditorInfo inputField) {
		if (inputField == null) {
			return false;
		}

		int inputType = inputField.inputType & InputType.TYPE_MASK_CLASS;
		int inputVariation = inputField.inputType & InputType.TYPE_MASK_VARIATION;

		return inputType == InputType.TYPE_CLASS_TEXT && inputVariation == InputType.TYPE_TEXT_VARIATION_FILTER;
	}

	/**
	 * isDialerField
	 * Dialer fields seem to take care of numbers and backspace on their own,
	 * so we need to be aware of them.
	 */
	public static boolean isDialerField(EditorInfo inputField) {
		return
			inputField != null
			&& inputField.inputType == InputType.TYPE_CLASS_PHONE
			&& inputField.packageName.equals("com.android.dialer");
	}


	/**
	 * determineInputModes
	 * Determine the typing mode based on the input field being edited. Returns an ArrayList of the allowed modes.
	 *
	 * @return ArrayList<SettingsStore.MODE_ABC | SettingsStore.MODE_123 | SettingsStore.MODE_PREDICTIVE>
	 */
	public static ArrayList<Integer> determineInputModes(EditorInfo inputField) {
		final int INPUT_TYPE_SHARP_007H_PHONE_BOOK = 65633;

		ArrayList<Integer> allowedModes = new ArrayList<>();

		if (inputField == null) {
			allowedModes.add(InputMode.MODE_123);
			return allowedModes;
		}

		if (
			inputField.inputType == INPUT_TYPE_SHARP_007H_PHONE_BOOK
			|| (
				inputField.privateImeOptions != null
				&& inputField.privateImeOptions.equals("io.github.sspanak.tt9.addword=true")
			)
		) {
			allowedModes.add(InputMode.MODE_123);
			allowedModes.add(InputMode.MODE_ABC);
			return allowedModes;
		}

		switch (inputField.inputType & InputType.TYPE_MASK_CLASS) {
			case InputType.TYPE_CLASS_NUMBER:
			case InputType.TYPE_CLASS_DATETIME:
				// Numbers and dates default to the symbols keyboard, with
				// no extra features.
			case InputType.TYPE_CLASS_PHONE:
				// Phones will also default to the symbols keyboard, though
				// often you will want to have a dedicated phone keyboard.
				allowedModes.add(InputMode.MODE_123);
				return allowedModes;

			case InputType.TYPE_CLASS_TEXT:
				// This is general text editing. We will default to the
				// normal alphabetic keyboard, and assume that we should
				// be doing predictive text (showing candidates as the
				// user types).
				if (!isSpecializedTextField(inputField)) {
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
	public static void determineTextCase(EditorInfo inputField) {
		// Logger.d("updateShift", "CM start: " + mCapsMode);
		// if (inputField != null && mCapsMode != SettingsStore.CASE_UPPER) {
		// 	int caps = 0;
		// 	if (inputField.inputType != InputType.TYPE_NULL) {
		// 		caps = currentInputConnection.getCursorCapsMode(inputField.inputType);
		// 	}
		// 	// mInputView.setShifted(mCapsLock || caps != 0);
		// 	// Logger.d("updateShift", "caps: " + caps);
		// 	if ((caps & TextUtils.CAP_MODE_CHARACTERS) == TextUtils.CAP_MODE_CHARACTERS) {
		// 		mCapsMode = SettingsStore.CASE_UPPER;
		// 	} else if ((caps & TextUtils.CAP_MODE_SENTENCES) == TextUtils.CAP_MODE_SENTENCES) {
		// 		mCapsMode = SettingsStore.CASE_CAPITALIZE;
		// 	} else if ((caps & TextUtils.CAP_MODE_WORDS) == TextUtils.CAP_MODE_WORDS) {
		// 		mCapsMode = SettingsStore.CASE_CAPITALIZE;
		// 	} else {
		// 		mCapsMode = SettingsStore.CASE_LOWER;
		// 	}
		// 	updateStatusIcon();
		// }
		// Logger.d("updateShift", "CM end: " + mCapsMode);
	}


	public static String getSurroundingWord(InputConnection currentInputConnection) {
		if (currentInputConnection == null) {
			return "";
		}

		String before = (String) currentInputConnection.getTextBeforeCursor(50, 0);
		String after = (String) currentInputConnection.getTextAfterCursor(50, 0);
		if (before == null || after == null) {
			return "";
		}

		Matcher beforeMatch = Pattern.compile("(\\w+)$").matcher(before);
		Matcher afterMatch = Pattern.compile("^(\\w+)").matcher(after);

		return (beforeMatch.find() ? beforeMatch.group(1) : "") + (afterMatch.find() ? afterMatch.group(1) : "");
	}
}
