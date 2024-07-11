package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftCommand32Key extends SoftKey {
	public SoftCommand32Key(Context context) { super(context); }
	public SoftCommand32Key(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftCommand32Key(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected void handleHold() {
		preventRepeat();
		if (validateTT9Handler()) {
			tt9.showTextManipulationPalette();
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
		return getContext().getString(R.string.virtual_key_text_manipulation).toUpperCase();
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
