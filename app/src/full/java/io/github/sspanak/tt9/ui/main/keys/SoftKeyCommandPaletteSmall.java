package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdCommandPalette;

public class SoftKeyCommandPaletteSmall extends BaseKeyboardResizeKey {
	private final CmdCommandPalette command = new CmdCommandPalette();

	public SoftKeyCommandPaletteSmall(Context context) { super(context); }
	public SoftKeyCommandPaletteSmall(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCommandPaletteSmall(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

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
