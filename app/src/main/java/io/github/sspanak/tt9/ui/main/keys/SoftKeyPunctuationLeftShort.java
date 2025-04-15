package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyPunctuationLeftShort extends SoftKeyPunctuationLeft {
	public SoftKeyPunctuationLeftShort(Context context) { super(context); }
	public SoftKeyPunctuationLeftShort(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuationLeftShort(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	@Override protected boolean isHiddenWhenLongSpace() { return false; }
}
