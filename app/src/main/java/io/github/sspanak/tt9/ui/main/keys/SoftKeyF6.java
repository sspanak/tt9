package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyF6 extends SoftKeyFn {
	public SoftKeyF6(Context context) {
		super(context);
	}

	public SoftKeyF6(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyF6(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected boolean handleRelease() {
		return tt9 != null && tt9.onKeyRedo(false);
	}
}
