package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyPunctuationRightShort extends SoftKeyPunctuationRight {
	public SoftKeyPunctuationRightShort(Context context) { super(context); }
	public SoftKeyPunctuationRightShort(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuationRightShort(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	@Override protected boolean isHiddenWhenLongSpace() { return false; }
}
