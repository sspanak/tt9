package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyLF2 extends SoftKey {
	public SoftKeyLF2(Context context) { super(context); }
	public SoftKeyLF2(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyLF2(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected void handleHold() {
		if (!validateTT9Handler()) {
			return;
		}

		preventRepeat();
		vibrate(Vibration.getHoldVibration());
		tt9.addWord();
	}

	@Override
	protected boolean handleRelease() {
		return validateTT9Handler() && tt9.onTab(false);
	}

	@Override
	protected String getTitle() {
		return "＋";
	}

	@Override
	protected String getSubTitle() {
		return getContext().getString(R.string.key_tab).toUpperCase();
	}

	@Override
	public void render() {
		super.render();
		if (tt9 != null) {
			setEnabled(!tt9.isVoiceInputActive());
		}
	}
}
