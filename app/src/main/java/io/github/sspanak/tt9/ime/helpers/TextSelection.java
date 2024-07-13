package io.github.sspanak.tt9.ime.helpers;

import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import androidx.annotation.Nullable;

public class TextSelection {
	@Nullable private final InputConnection connection;
	private int currentStart = 0;
	private int currentEnd = 0;


	public TextSelection(@Nullable InputConnection connection) {
		this.connection = connection;
	}


	public void onSelectionUpdate(int start, int end) {
		currentStart = start;
		currentEnd = end;
	}


	public void selectAll() {
		if (connection != null) {
			connection.performContextMenuAction(android.R.id.selectAll);
		}
	}


	public void selectNone() {
		if (connection != null) {
			connection.setSelection(currentEnd, currentEnd);
		}
	}


	public void selectChar(boolean backward) {
		if (connection != null) {
			connection.setSelection(currentStart, currentEnd + (backward ? -1 : 1));
		}
	}


	public void selectWord(boolean backward) {
		if (connection == null) {
			return;
		}

		connection.setSelection(currentStart, getNextWordPosition(backward));
	}


	public void copy() {

	}


	public void cut() {

	}


	public void paste() {

	}


	private int getNextWordPosition(boolean backward) {
		if (connection == null) {
			return currentEnd + (backward ? -1 : 1);
		}

		ExtractedText extractedText = connection.getExtractedText(new ExtractedTextRequest(), 0);
		if (extractedText == null) {
			return currentEnd + (backward ? -1 : 1);
		}

		int textLength = extractedText.text.length();
		int increment = backward ? -1 : 1;
		for (int i = currentEnd; i >= 0 && i < textLength; i += increment) {
			if (Character.isWhitespace(extractedText.text.charAt(i))) {
				return i + increment;
			}
		}

		return currentEnd + (backward ? -1 : 1);
	}
}
