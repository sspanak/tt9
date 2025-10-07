package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdCommandPalette;

public class SoftKeyCommandPalette extends SoftKeySettings {
	public SoftKeyCommandPalette(Context context) { super(context); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		return notSwiped() && CmdCommandPalette.run(tt9);
	}

	@Override
	protected int getNoEmojiTitle() {
		return new CmdCommandPalette().getIconText();
	}
}
