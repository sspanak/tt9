package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdCommandPalette;

public class SoftKeyCommandPalette extends SoftKeySettings {
	public SoftKeyCommandPalette(Context context) { super(context); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	public static boolean isMe(int keyId) {
		return keyId == R.id.soft_key_command_palette;
	}

	@Override
	protected boolean handleRelease() {
		return notSwiped() && new CmdCommandPalette().run(tt9);
	}

	@Override
	protected int getNoEmojiTitle() {
		return new CmdCommandPalette().getIconText();
	}
}
