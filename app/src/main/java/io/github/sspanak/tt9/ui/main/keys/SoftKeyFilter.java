package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyFilter extends BaseSoftKeyWithIcons {
	public SoftKeyFilter(Context context) { super(context); }
	public SoftKeyFilter(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyFilter(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

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

	@Override protected int getCentralIcon() { return R.drawable.ic_fn_filter; }
	@Override protected int getHoldIcon() { return R.drawable.ic_fn_filter_off; }

	@Override
	public void render() {
		if (tt9 != null) {
			setEnabled(
				tt9.isFilteringSupported()
				&& !tt9.isInputModeABC()
				&& !tt9.isInputModeNumeric()
				&& !tt9.isVoiceInputActive()
				&& !tt9.isTextEditingActive()
			);
		}

		super.render();
	}
}
