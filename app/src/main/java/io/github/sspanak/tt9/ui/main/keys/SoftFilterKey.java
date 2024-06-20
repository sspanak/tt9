package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SoftFilterKey extends SoftKey {
	public SoftFilterKey(Context context) { super(context); setFontSize(); }
	public SoftFilterKey(Context context, AttributeSet attrs) { super(context, attrs); setFontSize(); }
	public SoftFilterKey(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); setFontSize(); }

	private void setFontSize() {
		complexLabelTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_TITLE_RELATIVE_SIZE / 0.85f;
		complexLabelSubTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_SUB_TITLE_RELATIVE_SIZE / 0.85f;
	}

	@Override
	protected void handleHold() {
		preventRepeat();
		if (validateTT9Handler() && tt9.onKeyFilterClear(false)) {
			vibrate(Vibration.getHoldVibration());
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
