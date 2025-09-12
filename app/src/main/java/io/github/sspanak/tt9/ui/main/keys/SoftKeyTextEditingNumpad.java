package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyTextEditingNumpad extends SoftKeyFnNumpad {
	public SoftKeyTextEditingNumpad(Context context) { super(context); }
	public SoftKeyTextEditingNumpad(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTextEditingNumpad(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	protected boolean isFnPanelOn() {
		return isTextEditingActive();
	}


	protected void handleHold() {
		if (isTextEditingActive()) {
			preventRepeat();
		} else {
			super.handleHold();
		}
	}


	@Override protected String getTitle() {
		if (isTextEditingActive()) {
			return (getNumber() == 0) ? Characters.SPACE : "";
		}
		return super.getTitle();
	}
	@Override protected String getHoldText() { return isTextEditingActive() ? "" : super.getHoldText(); }


	@Override
	protected int getCentralIcon() {
		if (!isTextEditingActive()) {
			return -1;
		}

		final int keyId = getId();
		if (keyId == R.id.soft_key_1) return R.drawable.ic_dpad_left;
		if (keyId == R.id.soft_key_2) return R.drawable.ic_txt_select_none;
		if (keyId == R.id.soft_key_3) return R.drawable.ic_dpad_right;
		if (keyId == R.id.soft_key_4) return R.drawable.ic_txt_word_back;
		if (keyId == R.id.soft_key_5) return R.drawable.ic_txt_select_all;
		if (keyId == R.id.soft_key_6) return R.drawable.ic_txt_word_forward;
		if (keyId == R.id.soft_key_7) return R.drawable.ic_txt_cut;
		if (keyId == R.id.soft_key_8) return R.drawable.ic_txt_copy;
		if (keyId == R.id.soft_key_9) return R.drawable.ic_txt_paste;

		return -1;
	}


	private boolean isTextEditingActive() {
		return tt9 != null && tt9.isTextEditingActive();
	}
}
