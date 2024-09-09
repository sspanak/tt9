package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class SoftKeyOk extends SoftKey {

	public SoftKeyOk(Context context) {
		super(context);
	}

	public SoftKeyOk(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyOk(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected boolean handleRelease() {
		if (validateTT9Handler() && !tt9.onOK()) {
			// If no standard editor action was performed, it probably means we can only type a new line,
			// so we simulate the hardware ENTER key.
			tt9.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			return true;
		}

		return false;
	}


	@Override
	public void render() {
		super.render();
		if (tt9 != null) {
			setEnabled(!tt9.isVoiceInputActive());
		}
	}
}
