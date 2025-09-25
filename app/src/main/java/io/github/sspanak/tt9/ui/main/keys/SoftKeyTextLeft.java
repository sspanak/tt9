package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKeyTextLeft extends SoftKeyText {
	public SoftKeyTextLeft(Context context) { super(context); }
	public SoftKeyTextLeft(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTextLeft(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected String getKeyChar() {
		if (tt9 == null) return "";
		if (tt9.isInputModePhone()) return "*";
		if (tt9.isInputModeNumeric()) return ",";
		if (tt9.isTextEditingActive()) return "↶";

		return Characters.getChar(tt9.getLanguage(), SettingsStore.SOFT_KEY_TEXT_LEFT_DEFAULT);
	}

	@Override
	protected boolean handleRelease() {
		return isTextEditingOn() ? tt9.onKeyUndo(false) : super.handleRelease();
	}

	@Override
	protected String getTitle() {
		return isTextEditingOn() ? "" : super.getTitle();
	}

	@Override
	protected int getCentralIcon() {
		return isTextEditingOn() && !shouldHide() ? R.drawable.ic_fn_undo : -1;
	}
}
