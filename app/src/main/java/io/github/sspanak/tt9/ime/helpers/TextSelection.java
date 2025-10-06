package io.github.sspanak.tt9.ime.helpers;

import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.hacks.InputType;

public class TextSelection {
	@Nullable private final InputMethodService ims;
	private int currentStart = 0;
	private int currentEnd = 0;
	private final boolean isInputField;

	public TextSelection(@Nullable InputMethodService ims, @Nullable InputType inputType) {
		this.ims = ims;
		isInputField = inputType != null && !inputType.isLimited();
		detectCursorPosition();
	}


	private InputConnection getConnection() {
		return isInputField && ims != null ? ims.getCurrentInputConnection() : null;
	}


	public void onSelectionUpdate(int start, int end) {
		currentStart = start;
		currentEnd = end;
	}


	public boolean isEmpty() {
		return currentStart == currentEnd;
	}


	public void clear() {
		InputConnection connection = getConnection();
		if (connection != null) {
			connection.setSelection(currentEnd, currentEnd);
		}
	}


	public void clear(boolean backward) {
		InputConnection connection = getConnection();
		if (connection != null) {
			connection.setSelection(
				backward ? Math.min(currentStart, currentEnd) : Math.max(currentStart, currentEnd),
				backward ? Math.min(currentStart, currentEnd) : Math.max(currentStart, currentEnd)
			);
		}
	}


	public int length() {
		return Math.abs(currentEnd - currentStart);
	}


	public void selectAll() {
		InputConnection connection = getConnection();
		if (connection != null) {
			connection.performContextMenuAction(android.R.id.selectAll);
		}
	}


	public void selectNextChar(boolean backward) {
		InputConnection connection = getConnection();
		if (connection != null) {
			connection.setSelection(currentStart, currentEnd + (backward ? -1 : 1));
		}
	}


	public void selectNextWord(boolean backward) {
		InputConnection connection = getConnection();
		if (connection == null) {
			return;
		}

		connection.setSelection(currentStart, getNextWordPosition(backward));
	}


	private int getNextWordPosition(boolean backward) {
		InputConnection connection = getConnection();
		if (connection == null) {
			return currentEnd + (backward ? -1 : 1);
		}

		ExtractedText extractedText = connection.getExtractedText(new ExtractedTextRequest(), 0);
		if (extractedText == null) {
			return currentEnd + (backward ? -1 : 1);
		}

		int increment = backward ? -1 : 1;
		int textLength = extractedText.text.length();
		for (int ch = currentEnd + increment; ch >= 0 && ch < textLength; ch += increment) {
			if (!Character.isWhitespace(extractedText.text.charAt(ch))) {
				continue;
			}

			if (ch >= currentStart) {
				return ch;
			} else if (ch + 1 != currentEnd) {
				return ch + 1;
			}
		}

		return backward ? 0 : textLength;
	}


	private void detectCursorPosition() {
		ExtractedText extractedText = InputConnectionAsync.getExtractedText(getConnection(), new ExtractedTextRequest(), 0);
		if (extractedText != null && extractedText.text != InputConnectionAsync.TIMEOUT_SENTINEL) {
			currentStart = extractedText.startOffset + extractedText.selectionStart;
			currentEnd = extractedText.startOffset + extractedText.selectionEnd;
		}
	}


	public CharSequence getSelectedText() {
		ExtractedText extractedText = InputConnectionAsync.getExtractedText(getConnection(), new ExtractedTextRequest(), 0);
		if (extractedText == null || extractedText.text == InputConnectionAsync.TIMEOUT_SENTINEL) {
			return "";
		}


		int start = Math.min(extractedText.selectionStart, extractedText.selectionEnd);
		int end = Math.max(extractedText.selectionStart, extractedText.selectionEnd);
		return extractedText.text.subSequence(start, end);
	}
}
