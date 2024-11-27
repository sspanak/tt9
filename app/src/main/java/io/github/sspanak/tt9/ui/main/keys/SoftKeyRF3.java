package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyRF3 extends SoftKey {
	public SoftKeyRF3(Context context) { super(context); }
	public SoftKeyRF3(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyRF3(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected float getTitleRelativeSize() { return super.getTitleRelativeSize() / 0.85f; }
	@Override protected float getSubTitleRelativeSize() { return super.getSubTitleRelativeSize() / 0.85f; }

	private boolean isVoiceInputMissing() {
		return tt9 != null && tt9.isVoiceInputMissing();
	}

	private boolean isTextEditingMissing() {
		return tt9 != null && tt9.isInputLimited();
	}

	private boolean isTextEditingActive() {
		return tt9 != null && tt9.isTextEditingActive();
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
	protected String getTitle() {
		if (isTextEditingActive()) {
			return tt9 == null ? "ABC" : tt9.getABCString();
		}

		if (!isVoiceInputMissing()) {
			return "🎤";
		}

		return getContext().getString(R.string.virtual_key_text_editing).toUpperCase();
	}

	@Override
	protected String getSubTitle() {
		if (isTextEditingActive() || isTextEditingMissing() || isVoiceInputMissing()) {
			return null;
		}

		return getContext().getString(R.string.virtual_key_text_editing).toUpperCase();
	}

	@Override
	public void render() {
		super.render();
		setEnabled(!(isVoiceInputMissing() && isTextEditingMissing()));
	}
}
