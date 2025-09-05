package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyTextEditingSmall extends SoftKeyFnSmall {
	public SoftKeyTextEditingSmall(Context context) { super(context); }
	public SoftKeyTextEditingSmall(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTextEditingSmall(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean isVisible() {
		return (tt9 != null && tt9.isTextEditingActive() && getId() != R.id.soft_key_0) || super.isVisible();
	}

	@Override
	protected int getBottomIconId() {
		if (tt9 == null || !tt9.isTextEditingActive()) {
			return super.getBottomIconId();
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
}
