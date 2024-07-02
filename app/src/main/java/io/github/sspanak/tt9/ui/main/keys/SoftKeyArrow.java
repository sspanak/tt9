package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyArrow extends SoftKey {
	private boolean hold;

	public SoftKeyArrow(Context context) { super(context); }
	public SoftKeyArrow(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyArrow(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handlePress() {
		hold = false;
		return super.handlePress();
	}

	@Override
	protected void handleHold() {
		hold = true;
		moveCursor();
	}

	@Override
	protected boolean handleRelease() {
		if (hold) {
			hold = false;
			vibrate(Vibration.getReleaseVibration());
			return true;
		} else {
			return moveCursor();
		}
	}

	private boolean moveCursor() {
		if (!validateTT9Handler()) {
			return false;
		}

		int keyId = getId();
		if (keyId == R.id.soft_key_left_arrow) return onLeft();
		if (keyId == R.id.soft_key_right_arrow) return onRight();

		return false;
	}

	private boolean onLeft() {
		return tt9.onKeyScrollSuggestion(false, true) || tt9.onKeyMoveCursor(true);
	}

	private boolean onRight() {
		return tt9.onKeyScrollSuggestion(false, false) || tt9.onKeyMoveCursor(false);
	}
}
