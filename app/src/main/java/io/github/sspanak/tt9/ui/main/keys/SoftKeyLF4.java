package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyLF4 extends SwipeableKey {
	private final static float GLOBE_SIZE = 0.35f;

	public SoftKeyLF4(Context context) {
		super(context);
		complexLabelTitleSize = GLOBE_SIZE;
	}
	public SoftKeyLF4(Context context, AttributeSet attrs) {
		super(context, attrs);
		complexLabelTitleSize = GLOBE_SIZE;
	}
	public SoftKeyLF4(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		complexLabelTitleSize = GLOBE_SIZE;
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
		return "🌐";
	}

	@Override
	protected String getSubTitle() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && new Paint().hasGlyph("⌨")) {
			return "⌨";
		}

		return getContext().getString(R.string.virtual_key_input_mode).toUpperCase();
	}

	@Override
	public void render() {
		setTextSize(28);
		super.render();
		setEnabled(tt9 != null && !tt9.isVoiceInputActive());
	}
}
