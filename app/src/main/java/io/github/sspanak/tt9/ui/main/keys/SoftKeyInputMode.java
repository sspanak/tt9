package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.Vibration;

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
	protected void handleHold() {
		preventRepeat();

		if (validateTT9Handler()) {
			vibrate(Vibration.getHoldVibration());
			tt9.selectKeyboard();
		}
	}

	@Override
	protected boolean handleRelease() {
		return validateTT9Handler() && tt9.onKeyNextInputMode(false);
	}

	@Override
	protected int getNoEmojiTitle() {
		return R.string.virtual_key_input_mode;
	}

	@Override
	public void render() {
		super.render();
		if (tt9 != null) {
			setEnabled(!tt9.isVoiceInputActive());
		}
	}
}
