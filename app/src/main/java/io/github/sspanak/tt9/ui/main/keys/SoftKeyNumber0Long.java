package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyNumber0Long extends SoftKeyNumber0 {
	public SoftKeyNumber0Long(Context context) { super(context); }
	public SoftKeyNumber0Long(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber0Long(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	public static boolean isMe(int keyId) {
		return keyId == R.id.soft_key_200;
	}
}
