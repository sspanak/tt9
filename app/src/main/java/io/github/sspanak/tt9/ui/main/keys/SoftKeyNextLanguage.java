package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyNextLanguage extends SoftKey {
	public SoftKeyNextLanguage(Context context) { super(context); }
	public SoftKeyNextLanguage(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNextLanguage(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		return validateTT9Handler() && tt9.onKeyNextLanguage(false);
	}

	@Override
	public void render() {
		super.render();
		if (tt9 != null) {
			setEnabled(!tt9.isInputModeNumeric() && !tt9.isVoiceInputActive());
		}
	}
}
