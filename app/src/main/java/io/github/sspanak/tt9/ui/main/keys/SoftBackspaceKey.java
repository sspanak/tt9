package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftBackspaceKey extends SoftKey {
	private boolean hold;

	public SoftBackspaceKey(Context context) {
		super(context);
	}

	public SoftBackspaceKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftBackspaceKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	final protected boolean handlePress() {
		super.handlePress();
		hold = false;
		return deleteText();
	}

	@Override
	final protected void handleHold() {
		hold = true;
		deleteText();
	}

	@Override
	final protected boolean handleRelease() {
		vibrate(hold ? Vibration.getReleaseVibration() : Vibration.getNoVibration());
		hold = false;
		return true;
	}

	@Override
	final protected int getLongPressRepeatDelay() {
		return tt9 != null && tt9.getSettings().getBackspaceAcceleration() ? SettingsStore.SOFT_KEY_BACKSPACE_ACCELERATION_REPEAT_DELAY : super.getLongPressRepeatDelay();
	}

	private boolean deleteText() {
		if (validateTT9Handler() && !tt9.onBackspace(hold && tt9.getSettings().getBackspaceAcceleration())) {
			// Limited or special numeric field (e.g. formatted money or dates) cannot always return
			// the text length, therefore onBackspace() seems them as empty and does nothing. This results
			// in fallback to the default hardware key action. Here we simulate the hardware BACKSPACE.
			tt9.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			return true;
		}

		return false;
	}

	@Override
	protected int getNoEmojiTitle() {
		return R.string.virtual_key_del;
	}

	@Override
	protected String getTitle() {
		return LanguageKind.isRTL(tt9 != null ? tt9.getLanguage() : null) ? "⌦" : "⌫";
	}

	@Override
	public void render() {
		super.render();
		if (tt9 != null) {
			setEnabled(!tt9.isVoiceInputActive());
		}
	}
}
