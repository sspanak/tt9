package io.github.sspanak.tt9.ui.main.keys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

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
		init();
	}


	public BaseClickableKey(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	public BaseClickableKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
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


	/**
	 * Determines whether this key should use two-step activation in accessibility mode: first focus
	 * the key so its action is announced, then require a second action to activate it.
	 *
	 * Returning {@code false} disables two-step activation and allows direct activation. Subclasses
	 * for the numeric {@code 0-9} keys may override this to return {@code false}, because requiring
	 * an extra activation step for character entry adds unnecessary friction. For most other keys,
	 * keeping two-step activation enabled is preferable so the action can be announced before
	 * activation.
	 */
	protected boolean allowTwoStepInAccessibility() {
		return true;
	}


	@Override
	public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setLongClickable(false);
	}


	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (tt9 != null && tt9.isTouchExplorationEnabled() && allowTwoStepInAccessibility()) {
			return true;
		}

		int action = (event.getAction() & MotionEvent.ACTION_MASK);

		if (action == MotionEvent.ACTION_DOWN) {
			super.onTouchEvent(event);
			return handlePress();
		} else if (action == MotionEvent.ACTION_UP) {
			if (!repeat || hold) {
				return performClick();
			}
			repeat = false;
		}

		super.onTouchEvent(event);
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



	@SuppressLint("AccessibilityFocus")
	@Override
	public boolean onHoverEvent(MotionEvent event) {
		if (tt9 != null && tt9.isTouchExplorationEnabled() && !allowTwoStepInAccessibility()) {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_HOVER_ENTER:
					// Immediately activate the key for touch-exploration users. Use performClick() so
					// accessibility click events are still sent.
					handlePress();
					performClick();

					// Return true, to prevent incorrect announcements, such as "actions available", and
					// "double tap to activate".
					return true;
				case MotionEvent.ACTION_HOVER_EXIT:
					// Nothing to do here: we've already handled activation on HOVER_ENTER.
					// Consume it to avoid extra system behavior.
					return true;
			}
		}

		// Non-accessibility / normal interaction: fall back to default hover handling.
		return super.onHoverEvent(event);
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
		if (validateTT9Handler() && getVisibility() == VISIBLE) {
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


	@Override
	public boolean performClick() {
		super.performClick();

		hold = false;
		repeat = false;
		boolean result = handleRelease();
		lastPressedKey = ignoreLastPressedKey ? -1 : getId();
		return result;
	}


	protected void vibrate(int vibrationType) {
		if (tt9 != null) {
			vibration = vibration == null ? new Vibration(tt9.getSettings(), this) : vibration;
			vibration.vibrate(vibrationType);
		}
	}
}
