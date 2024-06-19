package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeySettings extends SoftKey {
	public SoftKeySettings(Context context) { super(context); }
	public SoftKeySettings(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeySettings(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected int getNoEmojiTitle() {
		return R.string.virtual_key_settings;
	}
}
