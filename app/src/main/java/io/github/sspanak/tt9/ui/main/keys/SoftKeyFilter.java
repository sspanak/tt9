package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyFilter extends SoftKey {
	public SoftKeyFilter(Context context) { super(context); }
	public SoftKeyFilter(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyFilter(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected float getTitleRelativeSize() { return super.getTitleRelativeSize() / 0.85f; }
	@Override protected float getSubTitleRelativeSize() { return super.getSubTitleRelativeSize() / 0.85f; }

	@Override
	protected void handleHold() {
		preventRepeat();
		if (validateTT9Handler() && tt9.onKeyFilterClear(false)) {
			vibrate(Vibration.getHoldVibration());
			ignoreLastPressedKey();
		}
	}

	@Override
	protected boolean handleRelease() {
		return
			validateTT9Handler()
			&& tt9.onKeyFilterSuggestions(false, getLastPressedKey() == getId());
	}

	@Override
	protected String getTitle() {
		return "CLR";
	}

	@Override
	protected String getSubTitle() {
		return "FLTR";
	}


	@Override
	public void render() {
		super.render();
		if (tt9 != null) {
			setEnabled(!tt9.isInputModeNumeric() && !tt9.isInputModeABC() && !tt9.isVoiceInputActive());
		}
	}
}
