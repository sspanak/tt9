package io.github.sspanak.tt9.ime.helpers;

import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
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
	private final ComposingSelection composingSelection;
	private int composingCursor = -1;
	private boolean nativeCursorRejected;

	private boolean isComposingSupported;
	private final boolean isNonText;
	private final boolean isUs;


	public TextField(@Nullable InputMethodService ims, @Nullable SettingsStore settings, EditorInfo inputField) {
		super(ims, inputField);
		composingSelection = new ComposingSelection(
			inputField != null ? inputField.initialSelStart : -1,
			inputField != null ? inputField.initialSelEnd : -1
		);

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
				getStringBeforeCursor(SettingsStore.AUTO_ASSISTANCE_BEFORE_TEXT),
				getStringAfterCursor(SettingsStore.AUTO_ASSISTANCE_AFTER_TEXT)
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
			composingSelection.recompose(text.length(), text.length(), SystemClock.uptimeMillis());
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
			composingSelection.recompose(parts[0].length(), word.length(), SystemClock.uptimeMillis());
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
			composingCursor = -1;
			composingSelection.replace(text.length(), 1, false, SystemClock.uptimeMillis());
			composingSelection.finish(SystemClock.uptimeMillis());
			connection.commitText(text, 1);
			composingText = "";
		}
	}


	@NonNull
	public String getComposingText() {
		return composingText.toString();
	}


	/** Request a real cursor within composing text; -1 restores the usual end-of-word cursor. */
	public void setComposingCursor(int offset) { composingCursor = offset; }

	public boolean supportsComposingCursor() {
		return isComposingSupported && !nativeCursorRejected
			&& composingSelection.getComposingStart() >= 0 && getConnection() != null;
	}

	public boolean hasComposingCursor() { return composingCursor >= 0; }

	public int getComposingCursorOffset(int start, int end, int candidatesStart, int candidatesEnd) {
		return composingCursor >= 0 && supportsComposingCursor()
			? composingSelection.getCursorOffset(start, end, candidatesStart, candidatesEnd) : -1;
	}

	/** Returns true for our own selection writes, including delayed callbacks from an older write. */
	public boolean onSelectionUpdate(int oldStart, int oldEnd, int start, int end, int candidatesStart, int candidatesEnd) {
		return composingSelection.update(oldStart, oldEnd, start, end, candidatesStart, candidatesEnd, SystemClock.uptimeMillis());
	}

	public int getSelectionStart() { return composingSelection.getStart(); }
	public int getSelectionEnd() { return composingSelection.getEnd(); }


	/**
	 * setComposingText
	 * A fail-safe setter for composing text, which ignores NULL input.
	 */
	public void setComposingText(@Nullable CharSequence text, int position) {
		if (text == null) {
			return;
		}

		composingText = text;

		InputConnection connection = getConnection();
		if (connection != null && isComposingSupported) {
			boolean moveCursor = composingCursor >= 0 && supportsComposingCursor();
			int origin = composingSelection.getComposingStart();
			if (moveCursor) connection.beginBatchEdit();
			composingSelection.replace(text.length(), position, moveCursor, SystemClock.uptimeMillis());
			boolean written = connection.setComposingText(text, position);
			if (moveCursor) {
				int cursor = origin + Math.min(composingCursor, text.length());
				composingSelection.moveTo(cursor, SystemClock.uptimeMillis());
				if (!written || !connection.setSelection(cursor, cursor)) {
					// Nonstandard editors may compose but reject selection. Keep the previous visible
					// cursor behavior for this field instead of leaving an invisible editing position.
					nativeCursorRejected = true;
					String fallback = text.subSequence(0, Math.min(composingCursor, text.length()))
						+ "|" + text.subSequence(Math.min(composingCursor, text.length()), text.length());
					composingText = fallback;
					composingSelection.replace(fallback.length(), 1, true, SystemClock.uptimeMillis());
					connection.setComposingText(fallback, 1);
				}
				connection.endBatchEdit();
			}
		}
	}

	public void setComposingText(@Nullable CharSequence text) { setComposingText(text, 1); }


	/**
	 * replaceComposingText
	 * A compatibility setter for composing text that first erases the previous composing text and
	 * then sets the new one. Useful for apps with a non-standard InputConnection implementation.
	 * NULL input is ignored.
	 */
	public void replaceComposingText(@Nullable CharSequence text) {
		if (text == null) {
			return;
		}

		composingText = text;

		InputConnection connection = getConnection();
		if (connection != null && isComposingSupported) {
			connection.beginBatchEdit();
			composingSelection.replace(text.length(), 1, false, SystemClock.uptimeMillis());
			connection.commitText("", 0);
			connection.setComposingText(text, 1);
			connection.endBatchEdit();
		}
	}


	/**
	 * finishComposingText
	 * Finish composing text or do nothing if the text field is invalid.
	 */
	public void finishComposingText() {
		composingCursor = -1;
		composingSelection.finish(SystemClock.uptimeMillis());
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
