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
		if (hasLettersOnAllKeys()) {
			handleRelease();
			return;
		}

		preventRepeat();
		if (validateTT9Handler() && tt9.onKeyFilterClear(false)) {
			vibrate(Vibration.getHoldVibration());
			ignoreLastPressedKey();
		}
	}


	@Override
	protected boolean handleRelease() {
		if (!validateTT9Handler()) {
			return false;
		}

		if (hasLettersOnAllKeys()) {
			return tt9.onKeySpaceKorean(false);
		} else {
			return tt9.onKeyFilterSuggestions(false, getLastPressedKey() == getId());
		}
	}


	@Override protected String getTitle() { return hasLettersOnAllKeys() ? "␣" : ""; }
	@Override protected int getCentralIcon() { return hasLettersOnAllKeys() ? 0 : R.drawable.ic_fn_filter; }
	@Override protected int getHoldIcon() { return hasLettersOnAllKeys() ? 0 : R.drawable.ic_fn_filter_off; }

	@Override protected float getTitleScale() { return hasLettersOnAllKeys() ? 1.3f * Math.min(1, getTT9Height()) * getScreenScaleY() : super.getTitleScale(); }

	@Override
	public void render() {
		if (tt9 != null) {
			setEnabled(
				!tt9.isInputModeNumeric()
				&& !tt9.isVoiceInputActive()
				&& (!tt9.isInputModeABC() || hasLettersOnAllKeys())
				&& (
					hasLettersOnAllKeys()
					|| (tt9.isFilteringSupported() && !tt9.isTextEditingActive())
				)
			);
		}

		super.render();
	}
}
