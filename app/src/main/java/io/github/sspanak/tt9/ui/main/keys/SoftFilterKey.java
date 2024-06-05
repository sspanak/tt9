package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftFilterKey extends SoftKey {
	public SoftFilterKey(Context context) { super(context); }
	public SoftFilterKey(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftFilterKey(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleHold() {
		if (!validateTT9Handler()) {
			return false;
		}

		return tt9.onKeyFilterClear(false);
	}

	@Override
	protected boolean handleRelease() {
		boolean multiplePress = getLastPressedKey() == getId();
		return tt9.onKeyFilterSuggestions(false, multiplePress);
	}
}
