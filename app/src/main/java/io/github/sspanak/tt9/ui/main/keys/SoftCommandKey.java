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

	@Override protected void handleHold() {}

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
		boolean noIconSupport = Characters.noEmojiSupported();
		int keyId = getId();

		// command palette
		if (keyId == R.id.soft_key_0) return noIconSupport ? getTextSubTitle(R.string.virtual_key_change_keyboard) : "⌨";
		if (keyId == R.id.soft_key_1) return noIconSupport ? getTextSubTitle(R.string.virtual_key_settings) : "⚙";
		if (keyId == R.id.soft_key_2) return "＋";
		if (keyId == R.id.soft_key_3) return "🎤";
		if (keyId == R.id.soft_key_5) return noIconSupport ? getTextSubTitle(R.string.virtual_key_text_manipulation) : "✂";

		// text manipulation
		if (keyId == R.id.soft_key_101) return "⏮";
		if (keyId == R.id.soft_key_102) return "|X|";
		if (keyId == R.id.soft_key_103) return "⏭";
		if (keyId == R.id.soft_key_105) return "|v|";
		if (keyId == R.id.soft_key_107) return "cut";
		if (keyId == R.id.soft_key_108) return "copy";
		if (keyId == R.id.soft_key_109) return "paste";

		return null;
	}

	@Override
	protected int getNumber(int keyId) {
		if (keyId == R.id.soft_key_101) return 1;
		if (keyId == R.id.soft_key_102) return 2;
		if (keyId == R.id.soft_key_103) return 3;
		if (keyId == R.id.soft_key_105) return 5;
		if (keyId == R.id.soft_key_107) return 7;
		if (keyId == R.id.soft_key_108) return 8;
		if (keyId == R.id.soft_key_109) return 9;

		return super.getNumber(keyId);
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
