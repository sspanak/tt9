package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyFText extends SoftKeyFn {
	public SoftKeyFText(Context context) {
		super(context);
	}

	public SoftKeyFText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyFText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected String getTitle() {
		return "";
	}

	@Override
	protected int getCentralIcon() {
		int keyId = getId();

		if (keyId == R.id.soft_key_101) return R.drawable.ic_dpad_left;
		if (keyId == R.id.soft_key_102) return R.drawable.ic_txt_select_none;
		if (keyId == R.id.soft_key_103) return R.drawable.ic_dpad_right;
		if (keyId == R.id.soft_key_104) return R.drawable.ic_txt_word_back;
		if (keyId == R.id.soft_key_105) return R.drawable.ic_txt_select_all;
		if (keyId == R.id.soft_key_106) return R.drawable.ic_txt_word_forward;
		if (keyId == R.id.soft_key_107) return R.drawable.ic_txt_cut;
		if (keyId == R.id.soft_key_108) return R.drawable.ic_txt_copy;
		if (keyId == R.id.soft_key_109) return R.drawable.ic_txt_paste;

		return -1;
	}
}
