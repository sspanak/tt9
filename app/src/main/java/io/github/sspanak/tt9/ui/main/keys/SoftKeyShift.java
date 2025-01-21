package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SoftKeyShift extends SoftKey {
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
		return R.drawable.ic_fn_shift;
	}

	@Override
	protected boolean handleRelease() {
		return validateTT9Handler() && tt9.onKeyNextTextCase(false);
	}

	@Override
	public void render() {
		setEnabled(
			tt9 != null
			&& !tt9.isVoiceInputActive()
			&& !tt9.isInputModePhone()
			&& !tt9.isNumericModeSigned()
			&& !tt9.isTextEditingActive()
		);
		super.render();
	}
}
