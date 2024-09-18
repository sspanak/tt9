package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.Characters;

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
		if (keyId == R.id.soft_key_punctuation_1) {
			return getKey1Char();
		} else if (keyId == R.id.soft_key_punctuation_2) {
			return getKey2Char();
		}

		return "";
	}

	private String getKey1Char() {
		if (tt9.isInputModePhone()) return "*";
		if (tt9.isInputModeNumeric()) return ",";

		return "!";
	}

	private String getKey2Char() {
		if (tt9.isInputModePhone()) return "#";
		if (tt9.isInputModeNumeric()) return ".";

		if (LanguageKind.isArabic(tt9.getLanguage())) return "؟";
		if (LanguageKind.isGreek(tt9.getLanguage())) return Characters.GR_QUESTION_MARK;

		return "?";
	}
}
