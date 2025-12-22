package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.commands.CmdBack;
import io.github.sspanak.tt9.commands.Command;

public class SoftKeySettingsClassic extends SoftKeySettings {
	private final CmdBack back = new CmdBack();

	public SoftKeySettingsClassic(Context context) { super(context); }
	public SoftKeySettingsClassic(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeySettingsClassic(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@NonNull
	@Override
	protected Command getCommand() {
		return tt9 != null && tt9.isFnPanelVisible() ? back : super.getCommand();
	}
}
