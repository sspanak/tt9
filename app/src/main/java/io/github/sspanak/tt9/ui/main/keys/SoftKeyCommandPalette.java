package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyCommandPalette extends SoftKeySettings {
	public SoftKeyCommandPalette(Context context) { super(context); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		if (notSwiped() && validateTT9Handler()) {
			tt9.onKeyCommandPalette(false);
		}
		return true;
	}

	@Override
	protected int getNoEmojiTitle() {
		return R.string.virtual_key_command_palette;
	}
}
