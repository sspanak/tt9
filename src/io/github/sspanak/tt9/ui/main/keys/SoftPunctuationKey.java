package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;

public class SoftPunctuationKey extends SoftKey {
	public SoftPunctuationKey(Context context) {
		super(context);
	}

	public SoftPunctuationKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftPunctuationKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected boolean handleRelease() {
		return tt9.onText(getKeyChar());
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
		
		Language language = LanguageCollection.getLanguage(tt9.getApplicationContext(), tt9.getSettings().getInputLanguage());
		
		int keyId = getId();
		if (tt9.isInputModePhone()) {
			if (keyId == R.id.soft_key_punctuation_1) return "*";
			if (keyId == R.id.soft_key_punctuation_2) return "#";
		} else if (tt9.isInputModeNumeric()) {
			if (keyId == R.id.soft_key_punctuation_1) return ",";
			if (keyId == R.id.soft_key_punctuation_2) return ".";
		} else {
			if (keyId == R.id.soft_key_punctuation_1) return "!";
			if (language.isArabic()){
				if (keyId == R.id.soft_key_punctuation_2) return "؟";
			}
			else{
				if (keyId == R.id.soft_key_punctuation_2) return "?";
			}
		}

		return "";
	}
}
