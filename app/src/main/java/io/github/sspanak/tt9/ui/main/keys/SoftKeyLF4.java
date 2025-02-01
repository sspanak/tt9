package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyLF4 extends BaseSwipeableKey {
	public SoftKeyLF4(Context context) { super(context); }
	public SoftKeyLF4(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyLF4(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	private boolean areThereManyLanguages() {
		return tt9 != null && tt9.getSettings().getEnabledLanguageIds().size() > 1;
	}

	private boolean isKeySmall() {
		return Math.max(getTT9Height(), getTT9Width()) < 0.9f;
	}

	@Override
	protected void handleHold() {
		preventRepeat();
		if (validateTT9Handler() && tt9.onKeyNextLanguage(false)) {
			vibrate(Vibration.getHoldVibration());
		}
	}

	@Override
	protected boolean handleRelease() {
		return notSwiped() && validateTT9Handler() && tt9.onKeyNextInputMode(false);
	}

	@Override
	protected void handleEndSwipeX(float position, float delta) {
		if (validateTT9Handler()) {
			tt9.nextKeyboard();
		}
	}

	@Override
	protected void handleEndSwipeY(float position, float delta) {
		if (validateTT9Handler()) {
			tt9.selectKeyboard();
		}
	}

	@Override
	protected String getTitle() {
		return tt9 != null ? tt9.getInputModeName() : getContext().getString(R.string.virtual_key_input_mode);
	}

	@Override
	protected float getTitleScale() {
		return super.getTitleScale() * 0.9f;
	}

	@Override
	protected int getHoldIcon() {
		return areThereManyLanguages() ? R.drawable.ic_fn_next_language : -1;
	}

	@Override
	protected float getHoldElementScale() {
		return super.getHoldElementScale() * 0.75f;
	}

	@Override
	public boolean isHoldEnabled() {
		return tt9 != null && !tt9.isInputModeNumeric();
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
			&& !tt9.isTextEditingActive()
		);

		super.render();
	}
}
