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

	private boolean isTextEdtingActive() {
		return tt9 != null && tt9.isTextEditingActive();
	}

	@Override
	protected void handleHold() {
		preventRepeat();

		if (!validateTT9Handler() || isTextEdtingActive()) {
			return;
		}

		if (tt9.isVoiceInputActive()) {
			tt9.toggleVoiceInput();
		} else {
			tt9.showTextEditingPalette();
		}
	}

	@Override
	protected boolean handleRelease() {
		if (!validateTT9Handler()) {
			return false;
		}

		if (isTextEdtingActive()) {
			tt9.hideTextEditingPalette();
		} else {
			tt9.toggleVoiceInput();
		}
		return true;
	}

	@Override
	protected String getTitle() {
		if (isTextEdtingActive()) {
			if (tt9 == null) {
				return "ABC";
			} else if (tt9.isInputModeNumeric()) {
				return "123";
			} else if (tt9.getLanguage() != null) {
				return tt9.getLanguage().getAbcString().toUpperCase(tt9.getLanguage().getLocale());
			}
		}

		return isTextEditingMissing() && !isVoiceInputMissing() ? "🎤" : getContext().getString(R.string.virtual_key_text_editing).toUpperCase();
	}

	@Override
	protected String getSubTitle() {
		return isTextEdtingActive() || (!isVoiceInputMissing() && isTextEditingMissing()) ? null : "🎤";
	}

	@Override
	public void render() {
		if (isVoiceInputMissing() && isTextEditingMissing()) {
			setVisibility(INVISIBLE);
		} else {
			super.render();
		}
	}
}
