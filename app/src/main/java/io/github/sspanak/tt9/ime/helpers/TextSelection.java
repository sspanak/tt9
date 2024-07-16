package io.github.sspanak.tt9.ime.helpers;

import android.content.Context;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.util.Clipboard;

public class TextSelection {
	@Nullable private final InputConnection connection;
	private final Context context;
	private int currentStart = 0;
	private int currentEnd = 0;


	public TextSelection(Context context, @Nullable InputConnection connection) {
		this.context = context;
		this.connection = connection;
		detectCursorPosition();
	}


	public void onSelectionUpdate(int start, int end) {
		currentStart = start;
		currentEnd = end;
	}


	public boolean isEmpty() {
		return currentStart == currentEnd;
	}


	public void clear() {
		if (connection != null) {
			connection.setSelection(currentEnd, currentEnd);
		}
	}


	public void clear(boolean backward) {
		if (connection != null) {
			connection.setSelection(
				backward ? Math.min(currentStart, currentEnd) : Math.max(currentStart, currentEnd),
				backward ? Math.min(currentStart, currentEnd) : Math.max(currentStart, currentEnd)
			);
		}
	}


	public void selectAll() {
		if (connection != null) {
			connection.performContextMenuAction(android.R.id.selectAll);
		}
	}


	public void selectNextChar(boolean backward) {
		if (connection != null) {
			connection.setSelection(currentStart, currentEnd + (backward ? -1 : 1));
		}
	}


	public void selectNextWord(boolean backward) {
		if (connection == null) {
			return;
		}

		connection.setSelection(currentStart, getNextWordPosition(backward));
	}


	public boolean copy() {
		CharSequence selectedText = getSelectedText();
		if (selectedText.length() == 0) {
			return false;
		}

		Clipboard.copy(context, getSelectedText());
		return true;
	}


	public boolean cut(@NonNull TextField textField) {
		if (copy()) {
			textField.setText("");
			return true;
		}

		return false;
	}


	public void paste(@NonNull TextField textField) {
		String clipboardText = Clipboard.paste(context);
		if (!clipboardText.isEmpty()) {
			textField.setText(clipboardText);
		}
	}


	private int getNextWordPosition(boolean backward) {
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
		if (connection == null) {
			return;
		}

		ExtractedText extractedText = connection.getExtractedText(new ExtractedTextRequest(), 0);
		if (extractedText != null) {
			currentStart = extractedText.startOffset + extractedText.selectionStart;
			currentEnd = extractedText.startOffset + extractedText.selectionEnd;
		}
	}


	private CharSequence getSelectedText() {
		if (connection == null) {
			return "";
		}

		ExtractedText extractedText = connection.getExtractedText(new ExtractedTextRequest(), 0);
		if (extractedText == null) {
			return "";
		}


		int start = Math.min(extractedText.selectionStart, extractedText.selectionEnd);
		int end = Math.max(extractedText.selectionStart, extractedText.selectionEnd);
		return extractedText.text.subSequence(start, end);
	}
}
