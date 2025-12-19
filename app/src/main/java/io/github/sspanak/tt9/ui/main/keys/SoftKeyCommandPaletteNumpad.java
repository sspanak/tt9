package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.commands.CmdBack;
import io.github.sspanak.tt9.commands.CmdCommandPalette;
import io.github.sspanak.tt9.commands.Command;

public class SoftKeyCommandPaletteNumpad extends SoftKeySettings {
	@NonNull private final CmdCommandPalette showCommandPalette = new CmdCommandPalette();
	@NonNull private final CmdBack back = new CmdBack();

	public SoftKeyCommandPaletteNumpad(Context context) { super(context); }
	public SoftKeyCommandPaletteNumpad(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCommandPaletteNumpad(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@NonNull
	@Override
	public Command getCommand() {
		return tt9 != null && tt9.isFnPanelVisible() ? back : showCommandPalette;
	}
}
