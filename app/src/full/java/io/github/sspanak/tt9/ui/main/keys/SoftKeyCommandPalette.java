package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdCommandPalette;

public class SoftKeyCommandPalette extends BaseKeyboardResizeKey {
	private final CmdCommandPalette command = new CmdCommandPalette();

	public SoftKeyCommandPalette(Context context) { super(context); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCommandPalette(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		return super.handleRelease() || command.run(tt9);
	}

	@Override
	protected int getNoEmojiTitle() {
		return command.getIconText();
	}

	@Override
	protected String getTitle() {
		return command.getIconEmojiText();
	}
}
