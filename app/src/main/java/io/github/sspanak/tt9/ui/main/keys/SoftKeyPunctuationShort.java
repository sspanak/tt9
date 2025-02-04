package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyPunctuationShort extends SoftKeyPunctuation {
	public SoftKeyPunctuationShort(Context context) { super(context); }
	public SoftKeyPunctuationShort(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuationShort(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	@Override protected boolean isHiddenWhenLongSpace() { return false; }
}
