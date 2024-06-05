package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

public class SoftCommandKey extends SoftNumberKey {
	public SoftCommandKey(Context context) { super(context);}
	public SoftCommandKey(Context context, AttributeSet attrs) { super(context, attrs);}
	public SoftCommandKey(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr);}

	@Override
	protected String getTitle() {
		return getNumber(getId()) + "";
	}

	@Override
	protected String getSubTitle() {
		int number = getNumber(getId());

		switch (number) {
			case 0:
				return "⌨";
			case 1:
				return "⚙";
			case 2:
				return "＋";
			case 3:
				return "🎤";
//			case 5:
//				return "✂";
		}

		return null;
	}

	@Override
	public void render() {
		if (tt9 != null && tt9.isVoiceInputMissing() && getNumber(getId()) == 3) {
			setVisibility(GONE);
		} else {
			super.render();
		}
	}
}
