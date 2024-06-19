package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.util.Characters;

public class SoftBackspaceKey extends SoftKey {

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
		return handleHold();
	}

	@Override
	final protected boolean handleHold() {
		if (validateTT9Handler() && !tt9.onBackspace()) {
			// Limited or special numeric field (e.g. formatted money or dates) cannot always return
			// the text length, therefore onBackspace() seems them as empty and does nothing. This results
			// in fallback to the default hardware key action. Here we simulate the hardware BACKSPACE.
			tt9.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			return true;
		}

		return false;
	}

	@Override
	final protected boolean handleRelease() {
		return false;
	}

	@Override
	protected String getTitle() {
		if (tt9 != null) {
			setEnabled(!tt9.isVoiceInputActive());
		}

		if (Characters.noEmojiSupported()) {
			return "Del";
		}

		return LanguageKind.isRTL(tt9 != null ? tt9.getLanguage() : null) ? "⌦" : "⌫";
	}
}
