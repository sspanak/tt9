package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.chars.Characters;

abstract public class SoftKeyText extends BaseSwipeableKey {
	public SoftKeyText(Context context) { super(context); }
	public SoftKeyText(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyText(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected void initColors(@NonNull SettingsStore settings) {
		backgroundColor = settings.getKeyBackgroundColor();
		cornerElementColor = settings.getKeyCornerElementColor();
		rippleColor = settings.getKeyRippleColor();
		centralIconColor = textColor = settings.getKeyTextColor();
	}


	abstract protected String getKeyChar();


	protected boolean isTextEditingOn() {
		return tt9 != null && tt9.isTextEditingActive();
	}


	protected boolean shouldHide() {
		final boolean isLongSpaceKey = getId() == R.id.soft_key_text_201 || getId() == R.id.soft_key_text_202;
		final boolean isShapeLongSpace = tt9 != null && tt9.getSettings().isNumpadShapeLongSpace();
		final boolean isInputModeNumeric = tt9 != null && tt9.isInputModeNumeric();
		final boolean isFnPanelOn = tt9 != null && tt9.isFnPanelVisible();

		if (isInputModeNumeric || hasLettersOnAllKeys() || isFnPanelOn) {
			return isLongSpaceKey;
		}

		return isShapeLongSpace != isLongSpaceKey;
	}


	@Override
	protected boolean handleRelease() {
		return tt9 != null && tt9.onText(getKeyChar(), false);
	}


	@Override
	protected String getTitle() {
		return Characters.getCharReadable(getKeyChar());
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
		final boolean isHidden = shouldHide();
		setVisibility(isHidden ? GONE : VISIBLE);

		ViewParent parent = getParent();
		if (parent instanceof RelativeLayout) {
			((RelativeLayout) parent).setVisibility(isHidden ? RelativeLayout.GONE : RelativeLayout.VISIBLE);
		}

		super.render();
	}
}
