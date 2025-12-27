package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.commands.CmdShowTutorial;
import io.github.sspanak.tt9.commands.Command;

public class SoftKeyTutorial extends SoftKeySettings {
	@NonNull private final CmdShowTutorial tutorial = new CmdShowTutorial();

	public SoftKeyTutorial(Context context) { super(context); }
	public SoftKeyTutorial(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTutorial(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	@NonNull
	protected Command getCommand() {
		return tutorial.isAvailable(tt9) ? tutorial : super.getCommand();
	}

	@Override
	protected boolean isSwipeable() {
		return !tutorial.isAvailable(tt9) && super.isSwipeable();
	}
}
