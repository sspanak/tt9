package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyNumber0Long extends SoftKeyNumber0 {
	public SoftKeyNumber0Long(Context context) { super(context); }
	public SoftKeyNumber0Long(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumber0Long(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	@Override protected boolean isHiddenWhenLongSpace() { return false; }
}
