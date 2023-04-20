package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.Logger;

public class SoftBackspaceKey extends SoftKey {

	public SoftBackspaceKey(Context context) {
		super(context);
	}

	public SoftBackspaceKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftBackspaceKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	final protected boolean handlePress() {
		return handleHold();
	}

	@Override
	final protected boolean handleHold() {
		if (tt9 == null) {
			Logger.w(getClass().getCanonicalName(), "Traditional T9 handler is not set. Ignoring key press.");
			return false;
		}

		return tt9.onBackspace();
	}

	@Override
	final protected boolean handleRelease() {
		return false;
	}
}
