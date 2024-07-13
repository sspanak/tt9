package io.github.sspanak.tt9.ime.helpers;

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

//		ExtractedText extractedText = connection.getExtractedText(new ExtractedTextRequest(), 0);
//		if (extractedText == null) {
//			return;
//		}
//
//		int selectionStart = extractedText.startOffset + extractedText.selectionStart;
//		int cursorPosition = extractedText.startOffset + extractedText.selectionEnd;
//
//		cursorPosition = backward ? cursorPosition - 1 : cursorPosition + 1;
//
//		connection.setSelection(selectionStart, cursorPosition);
	}


	public void copy() {

	}


	public void cut() {

	}


	public void paste() {

	}
}
