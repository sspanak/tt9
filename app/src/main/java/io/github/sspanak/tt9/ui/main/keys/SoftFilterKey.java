package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SoftFilterKey extends SoftKey {
	public SoftFilterKey(Context context) { super(context); setFontSize(); }
	public SoftFilterKey(Context context, AttributeSet attrs) { super(context, attrs); setFontSize(); }
	public SoftFilterKey(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); setFontSize(); }

	private void setFontSize() {
		complexLabelTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_TITLE_SIZE / 0.85f;
		complexLabelSubTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_SUB_TITLE_SIZE / 0.85f;
	}

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

	@Override
	protected String getTitle() {
		return "CLR";
	}

	@Override
	protected String getSubTitle() {
		return "FLTR";
	}
}
