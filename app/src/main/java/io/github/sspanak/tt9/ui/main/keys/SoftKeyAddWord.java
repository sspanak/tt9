package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftKeyAddWord extends SoftKey {
	public SoftKeyAddWord(Context context) { super(context); }
	public SoftKeyAddWord(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyAddWord(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		if (validateTT9Handler()) {
			tt9.addWord();
			return true;
		}

		return false;
	}

	@Override
	protected String getTitle() {
		return "＋";
	}

	@Override
	public void render() {
		super.render();
		if (tt9 != null) {
			setEnabled(!tt9.isVoiceInputActive());
		}
	}
}
