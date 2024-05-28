package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyInputMode extends SoftKey {
	public SoftKeyInputMode(Context context) {
		super(context);
	}

	public SoftKeyInputMode(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyInputMode(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected boolean handleHold() {
		preventRepeat();

		if (validateTT9Handler()) {
			tt9.changeKeyboard();
			return true;
		}

		return false;
	}


	@Override
	protected boolean handleRelease() {
		return validateTT9Handler() && tt9.onKeyNextInputMode(false);
	}
}
