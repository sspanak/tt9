package io.github.sspanak.tt9.ime.helpers;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;

public class TextField extends InputField {
	private CharSequence composingText = "";
	private final boolean isComposingSupported;


	public TextField(InputConnection inputConnection, EditorInfo inputField) {
		super(inputConnection, inputField);

		InputType inputType = new InputType(inputConnection, inputField);
		isComposingSupported = !inputType.isNumeric() && !inputType.isLimited() && !inputType.isRustDesk();
	}


	public boolean isEmpty() {
		return getStringBeforeCursor(1).isEmpty() && getStringAfterCursor(1).isEmpty();
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


	@NonNull public String getWordBeforeCursor(int reduceLength) {
		if (getTextAfterCursor(1).startsWithWord()) {
			return "";
		}

		String before = getStringBeforeCursor();
		if (before.isEmpty() || !Character.isAlphabetic(before.charAt(before.length() - 1))) {
			return "";
		}

		int lastSpace = Math.max(before.lastIndexOf(' ') + 1, 0);
		int length = Math.max(Math.min(before.length(), before.length() - reduceLength), lastSpace);

		return before.substring(lastSpace, length);
	}


	/**
	 * Returns the length of the word before the cursor. If the cursor is inside a word, 0 is returned,
	 * because there is no full word before it. The scanning length is up to the maximum returned by
	 * getTextBeforeCursor().
	 */
	public int getWordBeforeCursorStart() {
		if (getTextAfterCursor(1).startsWithWord()) {
			return 0;
		}

		String before = getStringBeforeCursor();
		if (before.isEmpty()) {
			return 0;
		}

		int spaceShift = Math.max(before.lastIndexOf(' '), 0);
		return before.length() - spaceShift;
	}


	/**
	 * Deletes one character by emulating a backspace key event (useful for deleting emoji and Unicode
	 * characters); Or deletes a region of text at once (faster, but may leave half a surrogate pair).
	 */
	public void deleteChars(int numberOfChars) {
		if (numberOfChars == 1) {
			connection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
			connection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
		} else if (numberOfChars > 1) {
			connection.deleteSurroundingText(numberOfChars, 0);
		}
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
		composingText = text;
		if (text != null && connection != null && isComposingSupported) {
			connection.setComposingText(text, position);
		}
	}

	public void setComposingText(CharSequence text) { setComposingText(text, 1); }


	/**
	 * setComposingTextWithHighlightedStem
	 * <p>
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
		if (connection == null) {
			return;
		}

		if (isComposingSupported) {
			connection.finishComposingText();
		} else {
			connection.commitText(composingText, 1);
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


	public boolean moveCursor(boolean backward) {
		if (
			connection == null
			|| (backward && getStringBeforeCursor(1).isEmpty())
			|| (!backward && getStringAfterCursor(1).isEmpty())
		) {
			return false;
		}

		sendDownUpKeyEvents(backward ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);

		return true;
	}


	public boolean sendDownUpKeyEvents(int keyCode) {
		return sendDownUpKeyEvents(keyCode, false, false);
	}


	public boolean sendDownUpKeyEvents(int keyCode, boolean shift, boolean ctrl) {
	if (connection != null) {
			int metaState = shift ? KeyEvent.META_SHIFT_ON : 0;
			metaState |= ctrl ? KeyEvent.META_CTRL_ON : 0;
			KeyEvent downEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, metaState);
			KeyEvent upEvent = new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, metaState);
			return connection.sendKeyEvent(downEvent) && connection.sendKeyEvent(upEvent);
		}

		return false;
	}
}
