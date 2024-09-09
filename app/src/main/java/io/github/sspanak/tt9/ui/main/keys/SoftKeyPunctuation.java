package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.LanguageKind;

public class SoftKeyPunctuation extends SoftKey {
	public SoftKeyPunctuation(Context context) {
		super(context);
	}

	public SoftKeyPunctuation(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyPunctuation(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected boolean handleRelease() {
		return validateTT9Handler() && tt9.onText(getKeyChar(), false);
	}

	@Override
	protected String getTitle() {
		String keyChar = getKeyChar();
		switch (keyChar) {
			case "":
				return "PUNC";
			case "*":
				return "✱";
			default:
				return keyChar;
		}
	}

	private String getKeyChar() {
		if (!validateTT9Handler()) {
			return "";
		}

		int keyId = getId();
		if (tt9.isInputModePhone()) {
			if (keyId == R.id.soft_key_punctuation_1) return "*";
			if (keyId == R.id.soft_key_punctuation_2) return "#";
		} else if (tt9.isInputModeNumeric()) {
			if (keyId == R.id.soft_key_punctuation_1) return ",";
			if (keyId == R.id.soft_key_punctuation_2) return ".";
		} else {
			if (keyId == R.id.soft_key_punctuation_1) return "!";
			if (keyId == R.id.soft_key_punctuation_2) {
				return LanguageKind.isArabic(tt9.getLanguage()) ? "؟" : "?";
			}
		}

		return "";
	}
}
