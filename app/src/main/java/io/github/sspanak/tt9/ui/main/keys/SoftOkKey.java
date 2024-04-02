package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class SoftOkKey extends SoftKey {

	public SoftOkKey(Context context) {
		super(context);
	}

	public SoftOkKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftOkKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected boolean handleRelease() {
		return
			validateTT9Handler()
				&& tt9.onKeyDown(KeyEvent.KEYCODE_ENTER, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
				&& tt9.onKeyUp(KeyEvent.KEYCODE_ENTER, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
	}
}
