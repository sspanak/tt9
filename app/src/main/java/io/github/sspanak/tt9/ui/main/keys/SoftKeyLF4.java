package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdNextInputMode;
import io.github.sspanak.tt9.commands.CmdNextKeyboard;
import io.github.sspanak.tt9.commands.CmdNextLanguage;
import io.github.sspanak.tt9.commands.CmdSelectKeyboard;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyLF4 extends BaseSwipeableKey {
	public SoftKeyLF4(Context context) {
		super(context);
		isSwipeable = true;
	}
	public SoftKeyLF4(Context context, AttributeSet attrs) {
		super(context, attrs);
		isSwipeable = true;
	}
	public SoftKeyLF4(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		isSwipeable = true;
	}

	@Override
	protected void initColors(@NonNull SettingsStore settings) {
		backgroundColor = settings.getKeyLf4BackgroundColor();
		cornerElementColor = settings.getKeyLf4CornerElementColor();
		rippleColor = settings.getKeyLf4RippleColor();
		centralIconColor = textColor = settings.getKeyLf4TextColor();
	}

	private boolean areThereManyLanguages() {
		return tt9 != null && tt9.getSettings().getEnabledLanguageIds().size() > 1;
	}

	private boolean isKeySmall() {
		return Math.max(getTT9Height(), getTT9Width()) < 0.9f;
	}

	@Override
	protected void handleHold() {
		preventRepeat();
		if (new CmdNextLanguage().run(tt9)) {
			vibrate(Vibration.getHoldVibration());
		}
	}

	@Override
	protected boolean handleRelease() {
		return notSwiped() && new CmdNextInputMode().run(tt9);
	}

	@Override
	protected void handleEndSwipeX(float position, float delta) {
		new CmdNextKeyboard().run(tt9);
	}

	@Override
	protected void handleEndSwipeY(float position, float delta) {
		new CmdSelectKeyboard().run(tt9);
	}

	@Override
	protected String getTitle() {
		return tt9 != null ? tt9.getInputModeName() : getContext().getString(R.string.virtual_key_input_mode);
	}

	@Override
	protected float getTitleScale() {
		return super.getTitleScale() * (isBopomofo() && tt9.isInputModeABC() ? 0.63f : 0.9f);
	}

	@Override
	protected int getCornerIcon(int position) {
		return position == ICON_POSITION_TOP_RIGHT && areThereManyLanguages() ? new CmdNextLanguage().getIcon() : -1;
	}

	@Override
	protected float getCornerElementScale(int position) {
		if (position == ICON_POSITION_TOP_RIGHT) {
			return super.getCornerElementScale(ICON_POSITION_TOP_RIGHT) * 0.75f;
		}
		return super.getCornerElementScale(position);
	}

	@Override
	public boolean isHoldEnabled() {
		return tt9 != null && !tt9.isInputModeNumeric();
	}

	@Override
	public void setHeight(int height) {
		if (tt9 != null && tt9.getSettings().isMainLayoutNumpad() && tt9.getSettings().isNumpadShapeV()) {
			height = Math.round(height * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_OUTER);
		}

		super.setHeight(height);
	}

	@Override
	public void render() {
		if (tt9 != null && tt9.isInputModeNumeric()) {
			resetIconCache();
		}

		if (areThereManyLanguages() && isKeySmall()) {
			setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
			setPaddingRelative(0, 0, 0, 10);
		} else if (areThereManyLanguages()) {
			setPaddingRelative(0, 20, 0, 0);
			setGravity(Gravity.CENTER);
		} else {
			setGravity(Gravity.CENTER);
		}


		setEnabled(
			tt9 != null
			&& !tt9.isVoiceInputActive()
			&& !tt9.isNumericModeStrict()
			&& !tt9.isInputModePhone()
			&& !tt9.isFnPanelVisible()
		);

		super.render();
	}
}
