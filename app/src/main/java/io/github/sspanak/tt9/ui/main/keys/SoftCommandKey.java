package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Characters;

public class SoftCommandKey extends SoftNumberKey {
	public SoftCommandKey(Context context) { super(context);}
	public SoftCommandKey(Context context, AttributeSet attrs) { super(context, attrs);}
	public SoftCommandKey(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr);}

	@Override
	protected String getTitle() {
		return getNumber(getId()) + "";
	}


	private String getTextSubTitle(int resId) {
		setTextSize(SettingsStore.SOFT_KEY_TITLE_SIZE);
		return getContext().getString(resId);
	}

	@Override
	protected String getSubTitle() {
		int number = getNumber(getId());

		boolean noIconSupport = Characters.noEmojiSupported();

		switch (number) {
			case 0:
				return noIconSupport ? getTextSubTitle(R.string.virtual_key_change_keyboard) : "⌨";
			case 1:
				return noIconSupport ? getTextSubTitle(R.string.virtual_key_settings) : "⚙";
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
