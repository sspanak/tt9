package io.github.sspanak.tt9.ime;

import android.text.InputType;
import android.text.TextUtils;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;

import io.github.sspanak.tt9.preferences.T9Preferences;


public class InputFieldHelper {
	private static int INPUT_TYPE_SHARP_007H_PHONE_BOOK = 65633;


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
				|| variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
				|| variation == InputType.TYPE_TEXT_VARIATION_URI
				|| variation == InputType.TYPE_TEXT_VARIATION_FILTER
		);
	}


	/**
	 * isFilterTextField
	 * handle filter list cases... do not hijack DPAD center and make sure back's go through proper
	 *
	 * @param  inputField
	 * @return boolean
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
	 * determineInputModes
	 * Determine the typing mode based on the input field being edited. Returns an ArrayList of the allowed modes.
	 *
	 * @param  inputField
	 * @return ArrayList<T9Preferences.MODE_ABC | T9Preferences.MODE_123 | T9Preferences.MODE_PREDICTIVE>
	 */
	public static ArrayList<Integer> determineInputModes(EditorInfo inputField) {
		ArrayList<Integer> allowedModes = new ArrayList<Integer>();

		if (inputField == null) {
			allowedModes.add(T9Preferences.MODE_123);
			return allowedModes;
		}

		if (
			inputField.inputType == INPUT_TYPE_SHARP_007H_PHONE_BOOK
			|| (
				inputField.privateImeOptions != null
				&& inputField.privateImeOptions.equals("io.github.sspanak.tt9.addword=true")
			)
		) {
			allowedModes.add(T9Preferences.MODE_123);
			allowedModes.add(T9Preferences.MODE_ABC);
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
				allowedModes.add(T9Preferences.MODE_123);
				return allowedModes;

			case InputType.TYPE_CLASS_TEXT:
				// This is general text editing. We will default to the
				// normal alphabetic keyboard, and assume that we should
				// be doing predictive text (showing candidates as the
				// user types).
				if (!isSpecializedTextField(inputField)) {
					allowedModes.add(T9Preferences.MODE_PREDICTIVE);
				}

				// ↓ fallthrough to add ABC and 123 modes ↓

			default:
				// For all unknown input types, default to the alphabetic
				// keyboard with no special features.
				allowedModes.add(T9Preferences.MODE_123);
				allowedModes.add(T9Preferences.MODE_ABC);

				return allowedModes;
		}
	}


	/**
	 * Helper to update the shift state of our keyboard based on the initial
	 * editor state.
	 */
	public static void deterimineTextCase(EditorInfo inputField) {
		// Log.d("updateShift", "CM start: " + mCapsMode);
		// if (inputField != null && mCapsMode != T9Preferences.CASE_UPPER) {
		// 	int caps = 0;
		// 	if (inputField.inputType != InputType.TYPE_NULL) {
		// 		caps = currentInputConnection.getCursorCapsMode(inputField.inputType);
		// 	}
		// 	// mInputView.setShifted(mCapsLock || caps != 0);
		// 	// Log.d("updateShift", "caps: " + caps);
		// 	if ((caps & TextUtils.CAP_MODE_CHARACTERS) == TextUtils.CAP_MODE_CHARACTERS) {
		// 		mCapsMode = T9Preferences.CASE_UPPER;
		// 	} else if ((caps & TextUtils.CAP_MODE_SENTENCES) == TextUtils.CAP_MODE_SENTENCES) {
		// 		mCapsMode = T9Preferences.CASE_CAPITALIZE;
		// 	} else if ((caps & TextUtils.CAP_MODE_WORDS) == TextUtils.CAP_MODE_WORDS) {
		// 		mCapsMode = T9Preferences.CASE_CAPITALIZE;
		// 	} else {
		// 		mCapsMode = T9Preferences.CASE_LOWER;
		// 	}
		// 	updateStatusIcon();
		// }
		// Log.d("updateShift", "CM end: " + mCapsMode);
	}


	public static String getSurroundingWord(InputConnection currentInputConnection) {
		CharSequence before = currentInputConnection.getTextBeforeCursor(50, 0);
		CharSequence after = currentInputConnection.getTextAfterCursor(50, 0);
		int bounds = -1;
		if (!TextUtils.isEmpty(before)) {
			bounds = before.length() -1;
			while (bounds > 0 && !Character.isWhitespace(before.charAt(bounds))) {
				bounds--;
			}
			before = before.subSequence(bounds, before.length());
		}
		if (!TextUtils.isEmpty(after)) {
			bounds = 0;
			while (bounds < after.length() && !Character.isWhitespace(after.charAt(bounds))) {
				bounds++;
			}
			after = after.subSequence(0, bounds);
		}
		return before.toString().trim() + after.toString().trim();
	}
}
