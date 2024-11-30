package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyLF4 extends SwipeableKey {
	public SoftKeyLF4(Context context) { super(context); }
	public SoftKeyLF4(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyLF4(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected float getTitleRelativeSize() { return super.getTitleRelativeSize() / 0.85f; }
	@Override protected float getSubTitleRelativeSize() { return super.getSubTitleRelativeSize() / 0.85f; }

	private boolean areThereManyLanguages() {
		return tt9 != null && tt9.getSettings().getEnabledLanguageIds().size() > 1;
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

	protected String getHoldIcon() {
		return "🌐";
	}

	protected String getPressIcon() {
		return tt9 != null ? tt9.getInputModeName() : getContext().getString(R.string.virtual_key_input_mode);
	}

	@Override
	protected String getTitle() {
		return areThereManyLanguages() ? getHoldIcon() : getPressIcon();
	}

	@Override
	protected String getSubTitle() {
		return areThereManyLanguages() ? getPressIcon() : null;
	}

	@Override
	public void render() {
		setTitleDisabled(tt9 != null && tt9.isInputModeNumeric() && areThereManyLanguages());
		super.render();

		setEnabled(
			tt9 != null
			&& !tt9.isVoiceInputActive()
			&& !tt9.isNumericModeStrict()
			&& !tt9.isInputModePhone()
		);
	}
}
