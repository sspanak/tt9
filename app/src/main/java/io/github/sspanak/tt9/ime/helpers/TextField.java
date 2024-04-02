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

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;

public class TextField {
	public static final int TYPE_MULTILINE_TEXT = EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
	public static final int IME_ACTION_ENTER = EditorInfo.IME_MASK_ACTION + 1;

	private final InputConnection connection;
	private final EditorInfo field;


	public TextField(InputConnection inputConnection, EditorInfo inputField) {
		connection = inputConnection;
		field = inputField;
	}


	public boolean equals(InputConnection inputConnection, EditorInfo inputField) {
		return
			connection != null && connection == inputConnection
			&& field != null && field == inputField;
	}


	public boolean isThereText() {
		if (connection == null) {
			return false;
		}

		ExtractedText extractedText = connection.getExtractedText(new ExtractedTextRequest(), 0);
		return extractedText != null && extractedText.text.length() > 0;
	}


	/**
	 * determineInputModes
	 * Determine the typing mode based on the input field being edited. Returns an ArrayList of the allowed modes.
	 *
	 * @return ArrayList<SettingsStore.MODE_ABC | SettingsStore.MODE_123 | SettingsStore.MODE_PREDICTIVE>
	 */
	public ArrayList<Integer> determineInputModes(InputType inputType) {
		ArrayList<Integer> allowedModes = new ArrayList<>();

		if (field == null) {
			allowedModes.add(InputMode.MODE_123);
			return allowedModes;
		}

		// Calculators (only 0-9 and math) and Dialer (0-9, "#" and "*") fields
		// handle all input themselves, so we are supposed to pass through all key presses.
		// Note: A Dialer field is not a Phone number field.
		if (inputType.isSpecialNumeric()) {
			allowedModes.add(InputMode.MODE_PASSTHROUGH);
			return allowedModes;
		}

		switch (field.inputType & android.text.InputType.TYPE_MASK_CLASS) {
			case android.text.InputType.TYPE_CLASS_NUMBER:
			case android.text.InputType.TYPE_CLASS_DATETIME:
			case android.text.InputType.TYPE_CLASS_PHONE:
				// Numbers, dates and phone numbers default to the numeric keyboard,
				// with no extra features.
				allowedModes.add(InputMode.MODE_123);
				return allowedModes;

			case android.text.InputType.TYPE_CLASS_TEXT:
				// This is general text editing. We will default to the
				// normal alphabetic keyboard, and assume that we should
				// be doing predictive text (showing candidates as the
				// user types).
				if (!inputType.isPassword()) {
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


	public String getStringAfterCursor(int numberOfChars) {
		CharSequence character = connection != null ? connection.getTextAfterCursor(numberOfChars, 0) : null;
		return character != null ? character.toString() : "";
	}


	public String getStringBeforeCursor(int numberOfChars) {
		CharSequence character = connection != null ? connection.getTextBeforeCursor(numberOfChars, 0) : null;
		return character != null ? character.toString() : "";
	}


	/**
	 * getStringBeforeCursor
	 * A simplified helper that return up to 50 characters before the cursor and "just works".
	 */
	public String getStringBeforeCursor() {
		return getStringBeforeCursor(50);
	}


	public Text getTextAfterCursor(int numberOfChars) {
		return new Text(getStringAfterCursor(numberOfChars));
	}


	public Text getTextBeforeCursor() {
		return new Text(getStringBeforeCursor());
	}


	/**
	 * getSurroundingWord
	 * Returns the word next or around the cursor. Scanning length is up to 50 chars in each direction.
	 */
	@NonNull public String getSurroundingWord(Language language) {
		Text before = getTextBeforeCursor();
		Text after = getTextAfterCursor(50);

		// emoji
		boolean beforeEndsWithGraphics = before.endsWithGraphic();
		boolean afterStartsWithGraphics = after.startsWithGraphic();

		if (beforeEndsWithGraphics && afterStartsWithGraphics) {
			return before.leaveEndingGraphics() + after.leaveStartingGraphics();
		}

		if (afterStartsWithGraphics) {
			return after.leaveStartingGraphics();
		}

		if (beforeEndsWithGraphics) {
			return before.leaveEndingGraphics();
		}

		// text
		boolean keepApostrophe = false;
		boolean keepQuote = false;
		if (language != null) {
			// Hebrew and Ukrainian use the respective special characters as letters
			keepApostrophe = LanguageKind.isHebrew(language) || LanguageKind.isUkrainian(language);
			keepQuote = LanguageKind.isHebrew(language);
		}

		return before.subStringEndingWord(keepApostrophe, keepQuote) + after.subStringStartingWord(keepApostrophe, keepQuote);
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
			stem.isEmpty() ? word : highlightText(word, 0, stem.length(), highlightMore)
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
		if (end <= start || start < 0) {
			Logger.w("tt9.util.highlightComposingText", "Cannot highlight invalid composing text range: [" + start + ", " + end + "]");
			return word;
		}

		// nothing to highlight in: an empty string; after the last letter; in special characters or emoji, because it breaks them
		if (word == null || word.length() == 0 || word.length() <= start || !Character.isLetterOrDigit(word.charAt(0))) {
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

	/**
	 * getAction
	 * Returns the most appropriate action for the "OK" key. It could be "send", "act as ENTER key", "go (to URL)" and so on.
	 */
	public int getAction() {
		if (field == null) {
			return EditorInfo.IME_ACTION_NONE;
		}

		if (field.actionId == EditorInfo.IME_ACTION_DONE) {
			return IME_ACTION_ENTER;
		} else if (field.actionId > 0) {
			return field.actionId;
		}

		int standardAction = field.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
		switch (standardAction) {
			case EditorInfo.IME_ACTION_DONE:
			case EditorInfo.IME_ACTION_GO:
			case EditorInfo.IME_ACTION_NEXT:
			case EditorInfo.IME_ACTION_PREVIOUS:
			case EditorInfo.IME_ACTION_SEARCH:
			case EditorInfo.IME_ACTION_SEND:
				return standardAction;
			default:
				return IME_ACTION_ENTER;
		}
	}

	/**
	 * performAction
	 * Sends an action ID to the connected application. Usually, the action is determined with "this.getAction()".
	 * Note that it is up to the app to decide what to do or ignore the action ID.
	 */
	public boolean performAction(int actionId) {
		return connection != null && actionId != EditorInfo.IME_ACTION_NONE && connection.performEditorAction(actionId);
	}
}
