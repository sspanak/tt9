package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import io.github.sspanak.tt9.Logger;
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

	protected boolean handlePress(int keyId) {
		if (tt9 == null) {
			Logger.w(getClass().getCanonicalName(), "Traditional T9 handler is not set. Ignoring key press.");
			return false;
		}

		if (tt9.getSettings().getInputMode() == InputMode.MODE_123) {
			if (keyId == R.id.soft_key_punctuation_1) return tt9.onOtherKey(KeyEvent.KEYCODE_STAR);
			if (keyId == R.id.soft_key_punctuation_2) return tt9.onOtherKey(KeyEvent.KEYCODE_POUND);
		} else {
			if (keyId == R.id.soft_key_punctuation_1) return tt9.onText("!");
			if (keyId == R.id.soft_key_punctuation_2) return tt9.onText("?");
		}

		return true;
	}

	@Override
	protected String getTitle() {
		int keyId = getId();

		if (tt9.getSettings().getInputMode() == InputMode.MODE_123) {
			if (keyId == R.id.soft_key_punctuation_1) return "✱";
			if (keyId == R.id.soft_key_punctuation_2) return "#";
		} else {
			if (keyId == R.id.soft_key_punctuation_1) return "!";
			if (keyId == R.id.soft_key_punctuation_2) return "?";
		}

		return "PUNC";
	}
}
