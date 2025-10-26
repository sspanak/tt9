package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SoftKeyOk extends BaseSoftKeyCustomizable {
	public SoftKeyOk(Context context) { super(context); }
	public SoftKeyOk(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyOk(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected void initColors(@NonNull SettingsStore settings) {
		backgroundColor = settings.getKeyOkBackgroundColor();
		rippleColor = settings.getKeyOkRippleColor();
		textColor = settings.getKeyOkTextColor();
	}

	@Override protected String getTitle() {
		CharSequence layoutTitle = getText();
		return layoutTitle.length() == 0 ? "OK" : layoutTitle.toString();
	}

	@Override
	protected boolean handleRelease() {
		if (validateTT9Handler() && !tt9.onOK()) {
			// If no standard editor action was performed, it probably means we can only type a new line,
			// so we simulate the hardware ENTER key.
			tt9.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			return true;
		}

		return false;
	}

	@Override
	public void setHeight(int height) {
		if (tt9 != null && tt9.getSettings().isMainLayoutNumpad() && tt9.getSettings().isNumpadShapeV()) {
			height = Math.round(height * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_OUTER);
		}

		super.setHeight(height);
	}

	@Override
	protected float getTitleScale() {
		return tt9 != null && tt9.getSettings().isMainLayoutNumpad() ? super.getTitleScale() : 1;
	}

	@Override
	public void render() {
		if (tt9 != null) {
			setEnabled(!tt9.isVoiceInputActive());
		}
		super.render();
	}
}
