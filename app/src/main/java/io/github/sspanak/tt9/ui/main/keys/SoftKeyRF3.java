package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyRF3 extends BaseSoftKeyWithIcons {
	public SoftKeyRF3(Context context) { super(context); }
	public SoftKeyRF3(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyRF3(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private boolean isVoiceInputActive() { return tt9 != null && tt9.isVoiceInputActive(); }
	private boolean isVoiceInputMissing() { return tt9 != null && tt9.isVoiceInputMissing(); }
	private boolean isTextEditingActive() { return tt9 != null && tt9.isTextEditingActive(); }
	private boolean isTextEditingMissing() { return tt9 != null && tt9.isInputLimited(); }

	private boolean isKeySmall() {
		return getTT9Height() < 0.8f && getTT9Width() < 0.7f;
	}


	@Override
	protected void handleHold() {
		preventRepeat();

		if (!validateTT9Handler() || isTextEditingActive() || isVoiceInputMissing()) {
			return;
		}

		tt9.toggleVoiceInput();
	}


	@Override
	protected boolean handleRelease() {
		if (!validateTT9Handler() && isTextEditingMissing() && isVoiceInputMissing()) {
			return false;
		}

		if (tt9.isVoiceInputActive() || isTextEditingMissing()) {
			tt9.toggleVoiceInput();
		} else if (isTextEditingActive()) {
			tt9.hideTextEditingPalette();
		} else {
			tt9.showTextEditingPalette();
		}

		return true;
	}


	@Override
	protected int getCentralIcon() {
		if (isTextEditingActive()) {
			return R.drawable.ic_keyboard;
		}

		if (isVoiceInputActive() || (isTextEditingMissing() && !isVoiceInputMissing())) {
			return R.drawable.ic_fn_voice_off;
		}

		return R.drawable.ic_txt_cut;
	}


	@Override
	protected float getCentralIconScale() {
		float scale = 1;
		if (!isVoiceInputActive() && !isTextEditingActive() && !isTextEditingMissing()) {
			scale = isKeySmall() ? 0.7f : 0.8f;
		}

		return super.getCentralIconScale() * scale;
	}


	@Override
	protected int getHoldIcon() {
		if (isVoiceInputActive() || isTextEditingActive() || isTextEditingMissing() || isVoiceInputMissing()) {
			return -1;
		}

		return R.drawable.ic_fn_voice;
	}


	@Override
	public void render() {
		resetIconCache();
		setEnabled(!(isVoiceInputMissing() && isTextEditingMissing()));
		super.render();
	}
}
