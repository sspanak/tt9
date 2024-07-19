package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyF3 extends SoftCommandKey {
	public SoftKeyF3(Context context) {
		super(context);
	}

	public SoftKeyF3(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKeyF3(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected String getSubTitle() {
		return "🎤";
	}

	@Override
	public void render() {
		setVisibility(tt9 != null && tt9.isVoiceInputMissing() ? GONE : VISIBLE);
		super.render();
	}
}
