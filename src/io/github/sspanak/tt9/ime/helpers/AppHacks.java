package io.github.sspanak.tt9.ime.helpers;

import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.ime.modes.InputMode;

public class AppHacks {
	private final EditorInfo editorInfo;
	private final TextField textField;


	public AppHacks(EditorInfo inputField, TextField textField) {
		this.editorInfo = inputField;
		this.textField = textField;
	}


	private boolean isKindleInvertedTextField() {
		return editorInfo != null && editorInfo.inputType == 1 && editorInfo.packageName.contains("com.amazon.kindle");
	}


	public boolean setComposingTextWithHighlightedStem(@NonNull String word) {
		if (isKindleInvertedTextField()) {
			textField.setComposingText(word);
			return true;
		}

		return false;
	}


	public boolean onBackspace(InputMode inputMode) {
		if (isKindleInvertedTextField()) {
			inputMode.clearWordStem();
		}

		return false;
	}
}
