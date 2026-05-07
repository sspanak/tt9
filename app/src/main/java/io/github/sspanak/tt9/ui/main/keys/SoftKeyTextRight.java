package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdRedo;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyTextRight extends SoftKeyText {
	private final CmdRedo redo = new CmdRedo();

	public SoftKeyTextRight(Context context) { super(context); }
	public SoftKeyTextRight(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTextRight(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		return tt9 != null && tt9.isTextEditingActive() ? redo.run(tt9) : super.handleRelease();
	}

	@Override
	protected String getAccessibilityText() {
		return isTextEditingOn() ? redo.getName(tt9) : super.getAccessibilityText();
	}

	protected String getKeyChar() {
		if (tt9 == null) return "";
		if (tt9.isInputTypePhone()) return "#";
		if (tt9.isInputModeNumeric()) return ".";
		if (tt9.isTextEditingActive()) return CmdRedo.iconTxt;

		return Characters.getChar(tt9.getLanguage(), SettingsStore.SOFT_KEY_TEXT_RIGHT_DEFAULT);
	}

	@Override
	protected String getTitle() {
		return isTextEditingOn() ? "" : super.getTitle();
	}

	@Override
	protected int getCentralIcon() {
		return isTextEditingOn() && !shouldHide() ? redo.getIcon() : -1;
	}
}
