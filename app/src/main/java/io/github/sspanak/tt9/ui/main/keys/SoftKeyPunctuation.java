package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

abstract public class SoftKeyPunctuation extends SoftKey {
	public SoftKeyPunctuation(Context context) { super(context); }
	public SoftKeyPunctuation(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuation(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	abstract protected String getKeyChar();

	protected boolean isHiddenWhenLongSpace() {
		return
			tt9 != null
			&& tt9.getSettings().isNumpadShapeLongSpace()
			&& !tt9.isInputModeNumeric()
			&& !hasLettersOnAllKeys();
	}

	@Override
	protected boolean handleRelease() {
		return tt9 != null && tt9.onText(getKeyChar(), false);
	}

	@Override
	protected String getTitle() {
		String keyChar = getKeyChar();
		return switch (keyChar) {
			case "*" -> "✱";
			case Characters.ZH_QUESTION_MARK -> "?";
			case Characters.ZH_EXCLAMATION_MARK -> "!";
			default -> keyChar;
		};
	}

	@Override
	public void setHeight(int height) {
		if (tt9 != null && tt9.getSettings().isMainLayoutNumpad() && tt9.getSettings().isNumpadShapeV()) {
			height = Math.round(height * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_INNER);
		}

		super.setHeight(height);
	}

	@Override
	public void render() {
		if (isHiddenWhenLongSpace()) {
			setVisibility(GONE);
		} else {
			setVisibility(VISIBLE);
		}

		super.render();
	}
}
