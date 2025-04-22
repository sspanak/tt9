package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyF4 extends SoftKeyFn {
	public SoftKeyF4(Context context) {
		super(context);
	}

	public SoftKeyF4(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyF4(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected boolean handleRelease() {
		return tt9 != null && tt9.onKeyUndo(false);
	}
}
