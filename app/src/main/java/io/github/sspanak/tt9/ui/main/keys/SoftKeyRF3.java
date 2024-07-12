package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SoftKeyRF3 extends SoftKey {
	public SoftKeyRF3(Context context) { super(context); setFontSize(); }
	public SoftKeyRF3(Context context, AttributeSet attrs) { super(context, attrs); setFontSize(); }
	public SoftKeyRF3(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); setFontSize(); }

	private void setFontSize() {
		complexLabelTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_TITLE_RELATIVE_SIZE / 0.85f;
		complexLabelSubTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_SUB_TITLE_RELATIVE_SIZE / 0.85f;
	}

	@Override
	protected void handleHold() {
		preventRepeat();
		if (validateTT9Handler()) {
			tt9.showTextEditingPalette();
		}
	}

	@Override
	protected boolean handleRelease() {
		if (validateTT9Handler()) {
			tt9.toggleVoiceInput();
			return true;
		}
		return false;
	}

	@Override
	protected String getTitle() {
		return getContext().getString(R.string.virtual_key_text_editing).toUpperCase();
	}

	@Override
	protected String getSubTitle() {
		return "🎤";
	}

	@Override
	public void render() {
		if (tt9 != null && tt9.isVoiceInputMissing()) {
			setVisibility(INVISIBLE);
		} else {
			super.render();
		}
	}
}
