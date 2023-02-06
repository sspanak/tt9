package io.github.sspanak.tt9.ime.helpers;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.ime.modes.InputMode;


public class InputFieldHelper {
	private static final Pattern beforeCursorWordRegex = Pattern.compile("(\\w+)$");
	private static final Pattern afterCursorWordRegex = Pattern.compile("^(\\w+)");


	public static boolean isThereText(InputConnection currentInputConnection) {
		if (currentInputConnection == null) {
			return false;
		}

		ExtractedText extractedText = currentInputConnection.getExtractedText(new ExtractedTextRequest(), 0);
		return extractedText != null && extractedText.text.length() > 0;
	}


	/**
	 * isThereSpaceAhead
	 * Checks whether there is a space after the cursor.
	 */
	public static boolean isThereSpaceAhead(InputConnection inputConnection) {
		CharSequence after = inputConnection != null ? inputConnection.getTextAfterCursor(1, 0) : null;
		return after != null && after.equals(" ");
	}


	/**
	 * isDialerField
	 * Dialer fields seem to take care of numbers and backspace on their own,
	 * so we need to be aware of them.
	 *
	 * NOTE: A Dialer field is not the same as a Phone field in a phone book.
	 */
	public static boolean isDialerField(EditorInfo inputField) {
		return
			inputField != null
			&& inputField.inputType == InputType.TYPE_CLASS_PHONE
			&& inputField.packageName.equals("com.android.dialer");
	}


	public static boolean isEmailField(EditorInfo inputField) {
		if (inputField == null) {
			return false;
		}

		int variation = inputField.inputType & InputType.TYPE_MASK_VARIATION;

		return
			variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
			|| variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
	}


	/**
	 * isFilterField
	 * handle filter list cases... do not hijack DPAD center and make sure back's go through proper
	 */
	public static boolean isFilterField(EditorInfo inputField) {
		if (inputField == null) {
			return false;
		}

		int inputType = inputField.inputType & InputType.TYPE_MASK_CLASS;
		int inputVariation = inputField.inputType & InputType.TYPE_MASK_VARIATION;

		return inputType == InputType.TYPE_CLASS_TEXT && inputVariation == InputType.TYPE_TEXT_VARIATION_FILTER;
	}


	private static boolean isPasswordField(EditorInfo inputField) {
		if (inputField == null) {
			return false;
		}

		int variation = inputField.inputType & InputType.TYPE_MASK_VARIATION;

		return
			variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
			|| variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
			|| variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
	}


	private static boolean isPersonNameField(EditorInfo inputField) {
		return inputField != null && (inputField.inputType & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
	}


	public static boolean isSpecializedTextField(EditorInfo inputField) {
		return isEmailField(inputField) || isPasswordField(inputField) || isUriField(inputField);
	}


	private static boolean isUriField(EditorInfo inputField) {
		return inputField != null && (inputField.inputType & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_URI;
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
				if (!isPasswordField(inputField) && !isFilterField(inputField)) {
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
	public static int determineTextCase(InputConnection inputConnection, EditorInfo inputField) {
		if (inputField == null || inputConnection == null || inputField.inputType == InputType.TYPE_NULL) {
			return InputMode.CASE_UNDEFINED;
		}

		if (isSpecializedTextField(inputField)) {
			return InputMode.CASE_LOWER;
		}

		if (isPersonNameField(inputField)) {
			return InputMode.CASE_CAPITALIZE;
		}

		switch (inputField.inputType & InputType.TYPE_MASK_FLAGS) {
			case InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS:
				return InputMode.CASE_UPPER;
			case InputType.TYPE_TEXT_FLAG_CAP_WORDS:
				return InputMode.CASE_CAPITALIZE;
		}

		return InputMode.CASE_UNDEFINED;
	}


	public static String getSurroundingWord(InputConnection currentInputConnection) {
		if (currentInputConnection == null) {
			return "";
		}

		CharSequence before = currentInputConnection.getTextBeforeCursor(50, 0);
		CharSequence after = currentInputConnection.getTextAfterCursor(50, 0);
		if (before == null || after == null) {
			return "";
		}

		Matcher beforeMatch = beforeCursorWordRegex.matcher(before);
		Matcher afterMatch = afterCursorWordRegex.matcher(after);

		return (beforeMatch.find() ? beforeMatch.group(1) : "") + (afterMatch.find() ? afterMatch.group(1) : "");
	}


	/**
	 * deletePrecedingSpace
	 * Deletes the preceding space before the given word. The word must be before the cursor.
	 * No action is taken when there is double space or when it's the beginning of the text field.
	 */
	public static void deletePrecedingSpace(InputConnection inputConnection, String word) {
		if (inputConnection == null) {
			return;
		}

		String searchText = " " + word;

		inputConnection.beginBatchEdit();
		CharSequence beforeText = inputConnection.getTextBeforeCursor(searchText.length() + 1, 0);
		if (
			beforeText == null
			|| beforeText.length() < searchText.length() + 1
			|| beforeText.charAt(1) != ' ' // preceding char must be " "
			|| beforeText.charAt(0) == ' ' // but do nothing when there is double space
		) {
			inputConnection.endBatchEdit();
			return;
		}

		inputConnection.deleteSurroundingText(searchText.length(), 0);
		inputConnection.commitText(word, 1);

		inputConnection.endBatchEdit();
	}
}
