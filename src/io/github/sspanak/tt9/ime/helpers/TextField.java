package io.github.sspanak.tt9.ime.helpers;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.ime.modes.InputMode;

public class TextField {
	private static final Pattern beforeCursorWordRegex = Pattern.compile("(\\w+)(?!\n)$");
	private static final Pattern afterCursorWordRegex = Pattern.compile("^(?<!\n)(\\w+)");

	public final InputConnection connection;
	public final EditorInfo field;

	public TextField(InputConnection inputConnection, EditorInfo inputField) {
		connection = inputConnection;
		field = inputField;
	}


	public boolean isThereText() {
		if (connection == null) {
			return false;
		}

		ExtractedText extractedText = connection.getExtractedText(new ExtractedTextRequest(), 0);
		return extractedText != null && extractedText.text.length() > 0;
	}


	/**
	 * isThereSpaceAhead
	 * Checks whether there is a space after the cursor.
	 */
	public boolean isThereSpaceAhead() {
		CharSequence after = connection != null ? connection.getTextAfterCursor(1, 0) : null;
		return after != null && after.equals(" ");
	}


	/**
	 * determineInputModes
	 * Determine the typing mode based on the input field being edited. Returns an ArrayList of the allowed modes.
	 *
	 * @return ArrayList<SettingsStore.MODE_ABC | SettingsStore.MODE_123 | SettingsStore.MODE_PREDICTIVE>
	 */
	public ArrayList<Integer> determineInputModes(InputType inputType) {
		final int INPUT_TYPE_SHARP_007H_PHONE_BOOK = 65633;

		ArrayList<Integer> allowedModes = new ArrayList<>();

		if (field == null) {
			allowedModes.add(InputMode.MODE_123);
			return allowedModes;
		}

		if (
			field.inputType == INPUT_TYPE_SHARP_007H_PHONE_BOOK
			|| (
				field.privateImeOptions != null
				&& field.privateImeOptions.equals("io.github.sspanak.tt9.addword=true")
			)
		) {
			allowedModes.add(InputMode.MODE_123);
			allowedModes.add(InputMode.MODE_ABC);
			return allowedModes;
		}

		switch (field.inputType & android.text.InputType.TYPE_MASK_CLASS) {
			case android.text.InputType.TYPE_CLASS_NUMBER:
			case android.text.InputType.TYPE_CLASS_DATETIME:
				// Numbers and dates default to the symbols keyboard, with
				// no extra features.
			case android.text.InputType.TYPE_CLASS_PHONE:
				// Phones will also default to the symbols keyboard, though
				// often you will want to have a dedicated phone keyboard.
				allowedModes.add(InputMode.MODE_123);
				return allowedModes;

			case android.text.InputType.TYPE_CLASS_TEXT:
				// This is general text editing. We will default to the
				// normal alphabetic keyboard, and assume that we should
				// be doing predictive text (showing candidates as the
				// user types).
				if (!inputType.isPassword() && !inputType.isFilter()) {
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
	public int determineTextCase(InputType inputType) {
		if (connection == null || field == null || field.inputType == android.text.InputType.TYPE_NULL) {
			return InputMode.CASE_UNDEFINED;
		}

		if (inputType.isSpecialized()) {
			return InputMode.CASE_LOWER;
		}

		if (inputType.isPersonName()) {
			return InputMode.CASE_CAPITALIZE;
		}

		switch (field.inputType & android.text.InputType.TYPE_MASK_FLAGS) {
			case android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS:
				return InputMode.CASE_UPPER;
			case android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS:
				return InputMode.CASE_CAPITALIZE;
		}

		return InputMode.CASE_UNDEFINED;
	}


	/**
	 * getTextBeforeCursor
	 * A simplified helper that return up to 50 characters before the cursor and "just works".
	 */
	public String getTextBeforeCursor() {
		if (connection == null) {
			return "";
		}

		CharSequence before = connection.getTextBeforeCursor(50, 0);
		return before != null ? before.toString() : "";
	}


	/**
	 * getTextBeforeCursor
	 * A simplified helper that return up to 50 characters after the cursor and "just works".
	 */
	public String getTextAfterCursor() {
		if (connection == null) {
			return "";
		}

		CharSequence before = connection.getTextAfterCursor(50, 0);
		return before != null ? before.toString() : "";
	}


	/**
	 * getSurroundingWord
	 * Returns the word next or around the cursor. Scanning length is up to 50 chars in each direction.
	 */
	public String getSurroundingWord() {
		Matcher before = beforeCursorWordRegex.matcher(getTextBeforeCursor());
		Matcher after = afterCursorWordRegex.matcher(getTextAfterCursor());

		return (before.find() ? before.group(1) : "") + (after.find() ? after.group(1) : "");
	}


	/**
	 * deletePrecedingSpace
	 * Deletes the preceding space before the given word. The word must be before the cursor.
	 * No action is taken when there is double space or when it's the beginning of the text field.
	 */
	public void deletePrecedingSpace(String word) {
		if (connection == null) {
			return;
		}

		String searchText = " " + word;

		connection.beginBatchEdit();
		CharSequence beforeText = connection.getTextBeforeCursor(searchText.length() + 1, 0);
		if (
			beforeText == null
			|| beforeText.length() < searchText.length() + 1
			|| beforeText.charAt(1) != ' ' // preceding char must be " "
			|| beforeText.charAt(0) == ' ' // but do nothing when there is double space
		) {
			connection.endBatchEdit();
			return;
		}

		connection.deleteSurroundingText(searchText.length(), 0);
		connection.commitText(word, 1);

		connection.endBatchEdit();
	}


	/**
	 * setText
	 * A fail-safe setter that appends text to the field, ignoring NULL input.
	 */
	public void setText(String text) {
		if (text != null && connection != null) {
			connection.commitText(text, 1);
		}
	}


	/**
	 * setComposingText
	 * A fail-safe setter for composing text, which ignores NULL input.
	 */
	public void setComposingText(CharSequence text, int position) {
		if (text != null && connection != null) {
			connection.setComposingText(text, position);
		}
	}

	public void setComposingText(CharSequence text) { setComposingText(text, 1); }


	/**
	 * setComposingTextWithHighlightedStem
	 *
	 * Sets the composing text, but makes the "stem" substring bold. If "highlightMore" is true,
	 * the "stem" part will be in bold and italic.
	 */
	public void setComposingTextWithHighlightedStem(CharSequence word, String stem, boolean highlightMore) {
		setComposingText(
			stem.length() > 0 ? highlightText(word, 0, stem.length(), highlightMore) : word
		);
	}

	public void setComposingTextWithHighlightedStem(CharSequence word, InputMode inputMode) {
		setComposingTextWithHighlightedStem(word, inputMode.getWordStem(), inputMode.isStemFilterFuzzy());
	}


	/**
	 * finishComposingText
	 * Finish composing text or do nothing if the text field is invalid.
	 */
	public void finishComposingText() {
		if (connection != null) {
			connection.finishComposingText();
		}
	}


	/**
	 * highlightText
	 * Makes the characters from "start" to "end" bold. If "highlightMore" is true,
	 * the text will be in bold and italic.
	 */
	private CharSequence highlightText(CharSequence word, int start, int end, boolean highlightMore) {
		if (end < start || start < 0) {
			Logger.w("tt9.util.highlightComposingText", "Cannot highlight invalid composing text range: [" + start + ", " + end + "]");
			return word;
		}

		SpannableString styledWord = new SpannableString(word);

		// default underline style
		styledWord.setSpan(new UnderlineSpan(), 0, word.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		// highlight the requested range
		styledWord.setSpan(
			new StyleSpan(Typeface.BOLD),
			start,
			Math.min(word.length(), end),
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
		);

		if (highlightMore) {
			styledWord.setSpan(
				new StyleSpan(Typeface.BOLD_ITALIC),
				start,
				Math.min(word.length(), end),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
			);
		}

		return styledWord;
	}


	public int getAction() {
		// @todo: also handle field.ActionId
		//
		switch (field.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
        case EditorInfo.IME_ACTION_GO:
            return EditorInfo.IME_ACTION_GO;
        case EditorInfo.IME_ACTION_NEXT:
            return EditorInfo.IME_ACTION_NEXT;
        case EditorInfo.IME_ACTION_SEARCH:
            return EditorInfo.IME_ACTION_SEARCH;
        case EditorInfo.IME_ACTION_SEND:
            return EditorInfo.IME_ACTION_SEND;
        default:
            return -1;
    }
	}
}
