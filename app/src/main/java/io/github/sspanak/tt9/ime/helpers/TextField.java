package io.github.sspanak.tt9.ime.helpers;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Text;

public class TextField extends InputField {
	@NonNull private CharSequence composingText = "";

	private boolean isComposingSupported;
	private final boolean isNonText;
	private final boolean isUs;


	public TextField(@Nullable InputMethodService ims, @Nullable SettingsStore settings, EditorInfo inputField) {
		super(ims, inputField);

		InputType inputType = new InputType(ims, inputField);
		isComposingSupported =
			!inputType.isNumeric() && !inputType.isLimited() // unsupported input fields
			&& !inputType.isRustDesk() // not fixed by settings.getAutoDisableComposing()
			&& (settings == null || settings.getAllowComposingText()); // disabled in settings
		isNonText = !inputType.isText();
		isUs = inputType.isUs();
	}


	public boolean isEmpty() {
		final String after = getStringAfterCursor(1);
		final String before = getStringBeforeCursor(1);

		return
			(after.isEmpty() || after.equals(InputConnectionAsync.TIMEOUT_SENTINEL))
			&& (before.isEmpty() || before.equals(InputConnectionAsync.TIMEOUT_SENTINEL));
	}


	@NonNull public String getStringAfterCursor(int numberOfChars) {
		CharSequence chars = numberOfChars > 0 ? InputConnectionAsync.getTextAfterCursor(isUs, getConnection(), numberOfChars, 0) : null;
		return chars != null ? chars.toString() : "";
	}


	@NonNull public String getStringBeforeCursor(int numberOfChars) {
		CharSequence chars = numberOfChars > 0 ? InputConnectionAsync.getTextBeforeCursor(isUs, getConnection(), numberOfChars, 0) : null;
		return chars != null ? chars.toString() : "";
	}


	/**
	 * getStringBeforeCursor
	 * A simplified helper that return up to 50 characters before the cursor and "just works".
	 */
	@NonNull public String getStringBeforeCursor() {
		return getStringBeforeCursor(50);
	}


	/**
	 * getSurroundingStringForAutoAssistance
	 * Returns just enough characters for AutoSpace and AutoText case classes to perform their work.
	 * Elements:
	 * 	[0] - up to 50 characters before the cursor
	 * 	[1] - up to 2 characters after the cursor
	 * When auto-assistance is disabled for the given input mode, returns two empty strings.
	 */
	@NonNull public String[] getSurroundingStringForAutoAssistance(@NonNull SettingsStore settings, @Nullable InputMode mode) {
		if (settings.isAutoAssistanceOn(mode)) {
			return new String[] {
				getStringBeforeCursor(50),
				getStringAfterCursor(2)
			};
		} else {
			return new String[] { "", "" };
		}
	}


	/**
	 * Similar to getStringBeforeCursor(), but returns a Text object instead of a String. On
	 * InputConnection timeout, the Text will be empty.
	 */
	@NonNull public Text getTextAfterCursor(@Nullable Language language, int numberOfChars) {
		return new Text(language, getStringAfterCursor(numberOfChars));
	}


	/**
	 * Similar to getStringAfterCursor(), but returns a Text object instead of a String. On
	 * InputConnection timeout, the Text will be empty.
	 */
	@NonNull public Text getTextBeforeCursor(@Nullable Language language, int numberOfChars) {
		return new Text(language, getStringBeforeCursor(numberOfChars));
	}


	/**
	 * getSurroundingWord
	 * Returns the word next or around the cursor. Scanning length is up to 50 chars in each direction.
	 */
	@NonNull public String getSurroundingWord(Language language) {
		final String[] parts = getSurroundingWordParts(language);
		return parts[0] + parts[1];
	}


	/**
	 * getSurroundingWord
	 * Detects the word next to or around the cursor and returns its parts: [0] = before cursor,
	 * [1] = after cursor. Scanning length is up to 50 chars in each direction.
	 */
	@NonNull
	public String[] getSurroundingWordParts(@Nullable Language language) {
		// Hebrew and Ukrainian use the respective special characters as letters
		boolean keepApostrophe = LanguageKind.isHebrew(language) || LanguageKind.isUkrainian(language);
		boolean keepQuote = LanguageKind.isHebrew(language);

		final Text textBefore = getTextBeforeCursor(language, 50);
		final Text textAfter = getTextAfterCursor(language, 50);

		final String wordBefore = textBefore.subStringEndingAlphanumeric(keepApostrophe, keepQuote);
		final String wordAfter = textAfter.subStringStartingAlphanumeric(keepApostrophe, keepQuote);

		return new String[] { wordBefore,  wordAfter };
	}


	/**
	 * Returns the length of the first word before the cursor including any whitespace after it.
	 * If the cursor is inside a word, 0 is returned, because there is no full word before it.
	 * The scanning length is up to the maximum returned by getTextBeforeCursor().
	 */
	public int getPaddedWordBeforeCursorLength() {
		if (getTextAfterCursor(null, 1).startsWithWord()) {
			return 0;
		}

		Text before = getTextBeforeCursor(null, 50);
		if (before.isEmpty()) {
			return 0;
		}

		int whitespaceShift = Math.max(before.lastBoundaryIndex(SettingsStore.BACKSPACE_ACCELERATION_MAX_CHARS_NO_SPACE), 0);
		return Math.min(before.length() - whitespaceShift, (int) (SettingsStore.BACKSPACE_ACCELERATION_MAX_CHARS * 1.5));
	}


	/**
	 * Deletes one or more characters, by using the most appropriate method for the current input field.
	 * It can either send a delete key event to delete a single character or use the faster
	 * "deleteSurroundingText()" to delete a region of text or a Unicode character.
	 */
	public void deleteChars(Language language, int numberOfChars) {
		InputConnection connection = getConnection();
		if (numberOfChars <= 0 || connection == null) {
			return;
		}

		if (isNonText) {
			composingText = composingText.length() > 1 ? composingText.subSequence(0, composingText.length() - 1) : "";
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			return;
		}

		if (numberOfChars == 1) {
			// Make sure we don't break complex letters or emojis. (for example, 🏴󠁧󠁢󠁷󠁬󠁳󠁿 = 14 chars!)
			// However, if the connection lags, we still delete at least one char as a last resort.
			String before = getStringBeforeCursor(30);
			numberOfChars = before.equals(InputConnectionAsync.TIMEOUT_SENTINEL) ? 1 : new Text(language, before).lastGraphemeLength();
		}

		composingText = composingText.length() > numberOfChars ? composingText.subSequence(0, composingText.length() - numberOfChars) : "";
		connection.deleteSurroundingText(numberOfChars, 0);
	}


	/**
	 * deletePrecedingSpace
	 * Deletes the space before the given word. The word must be before the cursor.
	 * No action is taken when there is no such word.
	 */
	public void deletePrecedingSpace(String word) {
		InputConnection connection = getConnection();
		if (connection == null) {
			return;
		}

		String searchText = " " + word;

		connection.beginBatchEdit();
		String beforeText = getStringBeforeCursor(searchText.length());
		if (!beforeText.equals(searchText)) {
			connection.endBatchEdit();
			return;
		}

		connection.deleteSurroundingText(searchText.length(), 0);
		connection.commitText(word, 1);

		connection.endBatchEdit();
	}


	/**
	 * Adds a space before the given word if the word is before the cursor. No action is taken if
	 * there is no such word before the cursor.
	 */
	public void addPrecedingSpace(String word) {
		InputConnection connection = getConnection();
		if (connection == null) {
			return;
		}

		connection.beginBatchEdit();
		String beforeText = getStringBeforeCursor(word.length());
		if (!beforeText.equals(word)) {
			connection.endBatchEdit();
			return;
		}

		connection.deleteSurroundingText(word.length(), 0);
		connection.commitText(" " + word, 1);

		connection.endBatchEdit();
	}


	public void disableComposing() {
		isComposingSupported = false;
	}


	/**
	 * Erases the previous N characters and sets the given "text" as composing text. N is the length of
	 * the given "text". Returns "true" if the operation was successful, "false" otherwise.
	 */
	public boolean recompose(String text) {
		InputConnection connection = getConnection();
		if (text == null || connection == null || !isComposingSupported) {
			return false;
		}

		connection.beginBatchEdit();
		boolean success = connection.deleteSurroundingText(text.length(), 0) && connection.setComposingText(text, 1);
		connection.endBatchEdit();

		if (success) {
			composingText = text;
		}

		return success;
	}


	@NonNull
	public String recomposeSurroundingWord(@Nullable Language language) {
		InputConnection connection = getConnection();
		if (connection == null || !isComposingSupported) {
			return "";
		}

		connection.beginBatchEdit();
		final String[] parts = getSurroundingWordParts(language);
		final String word = parts[0] + parts[1];
		final boolean success = connection.deleteSurroundingText(parts[0].length(), parts[1].length()) && connection.setComposingText(word, 1);
		connection.endBatchEdit();

		if (success) {
			composingText = word;
		}

		return success ? word : "";
	}


	/**
	 * setText
	 * A fail-safe setter that appends text to the field, ignoring NULL input.
	 */
	public void setText(String text) {
		InputConnection connection = getConnection();
		if (text != null && connection != null) {
			connection.commitText(text, 1);
		}
	}


	@NonNull
	public String getComposingText() {
		return composingText.toString();
	}


	/**
	 * setComposingText
	 * A fail-safe setter for composing text, which ignores NULL input.
	 */
	public void setComposingText(CharSequence text, int position) {
		composingText = text;
		InputConnection connection = getConnection();
		if (text != null && connection != null && isComposingSupported) {
			connection.setComposingText(text, position);
		}
	}

	public void setComposingText(CharSequence text) { setComposingText(text, 1); }


	/**
	 * finishComposingText
	 * Finish composing text or do nothing if the text field is invalid.
	 */
	public void finishComposingText() {
		InputConnection connection = getConnection();
		if (connection == null) {
			return;
		}

		if (isComposingSupported) {
			connection.finishComposingText();
		} else {
			connection.commitText(composingText, 1);
			composingText = "";
		}
	}
}
