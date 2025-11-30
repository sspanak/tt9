package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdShowSettings;

public class SoftKeySettings extends BaseKeyboardResizeKey {
	private final CmdShowSettings command = new CmdShowSettings();

	public SoftKeySettings(Context context) { super(context); }
	public SoftKeySettings(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeySettings(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		return super.handleRelease() || command.run(tt9);
	}

	@Override
	protected int getCentralIcon() {
		return command.getIcon();
	}
}
