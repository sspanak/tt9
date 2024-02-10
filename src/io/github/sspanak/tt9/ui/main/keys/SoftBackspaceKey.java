package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.languages.Characters;
import io.github.sspanak.tt9.languages.Language;

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
		return validateTT9Handler() && tt9.onBackspace();
	}

	@Override
	final protected boolean handleRelease() {
		return false;
	}

	@Override
	protected String getTitle() {
		if (Characters.noEmojiSupported()) {
			return "Del";
		}

		Language language = getCurrentLanguage();
		return language != null && (language.isArabic() || language.isHebrew()) ? "⌦" : "⌫";
	}
}
