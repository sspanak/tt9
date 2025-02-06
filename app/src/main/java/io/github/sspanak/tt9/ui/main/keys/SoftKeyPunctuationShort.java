package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyPunctuationShort extends SoftKeyPunctuation {
	public SoftKeyPunctuationShort(Context context) { super(context); }
	public SoftKeyPunctuationShort(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuationShort(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected boolean isHiddenWhenLongSpace() { return false; }

	protected String getKeyChar() {
		if (!validateTT9Handler()) {
			return "";
		}

		int keyId = getId();
		if (keyId == R.id.soft_key_punctuation_201) {
			return getKey1Char();
		} else if (keyId == R.id.soft_key_punctuation_202) {
			return getKey2Char();
		}

		return super.getKeyChar();
	}
}
