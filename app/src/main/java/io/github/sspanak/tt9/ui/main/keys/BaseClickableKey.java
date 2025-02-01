package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;
import io.github.sspanak.tt9.util.Logger;

public class BaseClickableKey extends com.google.android.material.button.MaterialButton implements View.OnTouchListener, View.OnLongClickListener {
	private final String LOG_TAG = getClass().getSimpleName();

	protected TraditionalT9 tt9;
	protected Vibration vibration;

	private boolean hold = false;
	private boolean repeat = false;
	private long lastLongClickTime = 0;
	private final Handler repeatHandler = new Handler(Looper.getMainLooper());

	private static int lastPressedKey = -1;
	private boolean ignoreLastPressedKey = false;


	public BaseClickableKey(Context context) {
		super(context);
		setHapticFeedbackEnabled(false);
		setOnTouchListener(this);
		setOnLongClickListener(this);
	}


	public BaseClickableKey(Context context, AttributeSet attrs) {
		super(context, attrs);
		setHapticFeedbackEnabled(false);
		setOnTouchListener(this);
		setOnLongClickListener(this);
	}


	public BaseClickableKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setHapticFeedbackEnabled(false);
		setOnTouchListener(this);
		setOnLongClickListener(this);
	}


	public void setTT9(TraditionalT9 tt9) {
		this.tt9 = tt9;
	}


	protected boolean validateTT9Handler() {
		if (tt9 == null) {
			Logger.w(LOG_TAG, "Traditional T9 handler is not set. Ignoring key press.");
			return false;
		}

		return true;
	}


	@Override
	public boolean onTouch(View view, MotionEvent event) {
		super.onTouchEvent(event);

		int action = (event.getAction() & MotionEvent.ACTION_MASK);

		if (action == MotionEvent.ACTION_DOWN) {
			return handlePress();
		} else if (action == MotionEvent.ACTION_UP) {
			if (!repeat || hold) {
				hold = false;
				repeat = false;
				boolean result = handleRelease();
				lastPressedKey = ignoreLastPressedKey ? -1 : getId();
				return result;
			}
			repeat = false;
		}
		return false;
	}


	@Override
	public boolean onLongClick(View view) {
		// sometimes this gets called twice, so we debounce the call to the repeating function
		final long now = System.currentTimeMillis();
		if (now - lastLongClickTime < SettingsStore.SOFT_KEY_DOUBLE_CLICK_DELAY) {
			return false;
		}

		hold = true;
		lastLongClickTime = now;
		repeatOnLongPress();
		return true;
	}


	/**
	 * repeatOnLongPress
	 * Repeatedly calls "handleHold()" upon holding the respective SoftKey, to simulate physical keyboard behavior.
	 */
	private void repeatOnLongPress() {
		if (hold) {
			repeat = true;
			handleHold();
			lastPressedKey = ignoreLastPressedKey ? -1 : getId();
			repeatHandler.removeCallbacks(this::repeatOnLongPress);
			repeatHandler.postDelayed(this::repeatOnLongPress, SettingsStore.SOFT_KEY_REPEAT_DELAY);
		}
	}


	/**
	 * preventRepeat
	 * Prevents "handleHold()" from being called repeatedly when the SoftKey is being held.
	 */
	protected void preventRepeat() {
		hold = false;
		repeatHandler.removeCallbacks(this::repeatOnLongPress);
	}


	protected static int getLastPressedKey() {
		return lastPressedKey;
	}


	protected void ignoreLastPressedKey() {
		ignoreLastPressedKey = true;
	}


	protected boolean handlePress() {
		if (validateTT9Handler()) {
			vibrate(Vibration.getPressVibration(this));
		}

		return false;
	}


	protected void handleHold() {}


	protected boolean handleRelease() {
		return false;
	}


	public boolean isHoldEnabled() {
		return true;
	}


	protected void vibrate(int vibrationType) {
		if (tt9 != null) {
			vibration = vibration == null ? new Vibration(tt9.getSettings(), this) : vibration;
			vibration.vibrate(vibrationType);
		}
	}
}
