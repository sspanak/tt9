package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyFn extends SoftKeyNumber {
	public SoftKeyFn(Context context) { super(context);}
	public SoftKeyFn(Context context, AttributeSet attrs) { super(context, attrs);}
	public SoftKeyFn(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr);}

	@Override protected void handleHold() { preventRepeat(); }
	@Override protected String getTitle() { return getNumber(getId()) + ""; }
	@Override protected String getSubTitle() { return null; }

	@Override
	protected int getNumber(int keyId) {
		if (keyId == R.id.soft_key_101) return 1;
		if (keyId == R.id.soft_key_102) return 2;
		if (keyId == R.id.soft_key_103) return 3;
		if (keyId == R.id.soft_key_104) return 4;
		if (keyId == R.id.soft_key_105) return 5;
		if (keyId == R.id.soft_key_106) return 6;
		if (keyId == R.id.soft_key_107) return 7;
		if (keyId == R.id.soft_key_108) return 8;
		if (keyId == R.id.soft_key_109) return 9;

		return super.getNumber(keyId);
	}
}
