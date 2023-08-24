package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;

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
	protected boolean handleHold() {
		if (tt9 == null || tt9.getInputMode() != InputMode.MODE_123) {
			return super.handleHold();
		}

		preventRepeat();
		int keyId = getId();
		if (keyId == R.id.soft_key_punctuation_1) return tt9.onText(",");
		if (keyId == R.id.soft_key_punctuation_2) return tt9.onText(".");

		return false;
	}

	@Override
	protected boolean handleRelease() {
		if (!validateTT9Handler()) {
			return false;
		}

		int keyId = getId();
		if (tt9.getInputMode() == InputMode.MODE_123) {
			if (keyId == R.id.soft_key_punctuation_1) return tt9.onText("*");
			if (keyId == R.id.soft_key_punctuation_2) return tt9.onText("#");
		} else {
			if (keyId == R.id.soft_key_punctuation_1) return tt9.onText("!");
			if (keyId == R.id.soft_key_punctuation_2) return tt9.onText("?");
		}

		return true;
	}

	@Override
	protected String getTitle() {
		if (tt9 == null) {
			return "PUNC";
		}

		int keyId = getId();
		if (tt9.getInputMode() == InputMode.MODE_123) {
			if (keyId == R.id.soft_key_punctuation_1) return "✱";
			if (keyId == R.id.soft_key_punctuation_2) return "#";
		} else {
			if (keyId == R.id.soft_key_punctuation_1) return "!";
			if (keyId == R.id.soft_key_punctuation_2) return "?";
		}

		return "PUNC";
	}

	@Override
	protected String getSubTitle() {
		int keyId = getId();
		if (tt9 != null && tt9.getInputMode() == InputMode.MODE_123) {
			if (keyId == R.id.soft_key_punctuation_1) return ",";
			if (keyId == R.id.soft_key_punctuation_2) return ".";
		}

		return null;
	}
}
