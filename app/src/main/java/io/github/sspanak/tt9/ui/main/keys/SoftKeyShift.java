package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;

public class SoftKeyShift extends BaseSoftKeyWithIcons {
	public SoftKeyShift(Context context) {
		super(context);
	}

	public SoftKeyShift(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyShift(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override protected int getCentralIcon() {
		final int textCase = tt9 != null ? tt9.getDisplayTextCase(tt9.getLanguage(), tt9.getTextCase()) : InputMode.CASE_UNDEFINED;
		return switch (textCase) {
			case InputMode.CASE_CAPITALIZE -> R.drawable.ic_fn_shift_caps;
			case InputMode.CASE_UPPER -> R.drawable.ic_fn_shift_up;
			default -> R.drawable.ic_fn_shift_low;
		};
	}

	@Override
	protected boolean handleRelease() {
		return validateTT9Handler() && tt9.onKeyNextTextCase(false);
	}

	@Override
	public void render() {
		resetIconCache();
		setEnabled(
			tt9 != null
			&& tt9.getLanguage() != null && tt9.getLanguage().hasUpperCase()
			&& !tt9.isVoiceInputActive()
			&& !tt9.isInputModePhone()
			&& !tt9.isNumericModeSigned()
			&& !tt9.isTextEditingActive()
		);
		super.render();
	}
}
