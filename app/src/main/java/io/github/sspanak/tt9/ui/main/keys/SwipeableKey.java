package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Timer;

abstract public class SwipeableKey extends SoftKey {
	private static final String LOG_TAG = SwipeableKey.class.getSimpleName();

	private float HOLD_DURATION_THRESHOLD;
	protected float SWIPE_X_THRESHOLD;
	protected float SWIPE_Y_THRESHOLD;

	private boolean isHolding = false;
	private boolean isSwipingX = false;
	private boolean isSwipingY = false;
	private boolean notSwiped = false;

	private float startX;
	private float startY;
	private long startTime;

	private int swipeCount = 0;
	private long swipeProcessingTime = 0;
	private long swipeProcessingTimeAverage = 40;


	public SwipeableKey(Context context) {
		super(context);
	}


	public SwipeableKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public SwipeableKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	private void resetTimeThreshold() {
		HOLD_DURATION_THRESHOLD = getHoldDurationThreshold();
	}


	private void initSwipeThresholds() {
		if (SWIPE_X_THRESHOLD == 0) {
			SWIPE_X_THRESHOLD = getSwipeXThreshold();
		}

		if (SWIPE_Y_THRESHOLD == 0) {
			SWIPE_Y_THRESHOLD = getSwipeYThreshold();
		}
	}


	protected float getHoldDurationThreshold() { return SettingsStore.SOFT_KEY_REPEAT_DELAY * 9; }
	protected float getSwipeYThreshold() { return getResources().getDimensionPixelSize(R.dimen.numpad_key_height) / 10.0f; }


	/**
	 * Calculate the minimum amount of finger movement to be considered a swipe. It is meant
	 * to prevent accidental swipes when pressing or holding the key.
	 */
	protected float getSwipeXThreshold() {
		// If the key width is not available, use the old method. It's better than nothing.
		if (tt9 == null || tt9.getWidth() == 0) {
			return getSwipeYThreshold();
		}

		try {
			// The simpler getResource.getFloat() requires API 29, so we must get the value manually.
			TypedValue outValue = new TypedValue();
			getResources().getValue(R.dimen.numpad_function_key_layout_weight, outValue, true);
			float functionKeyScale = outValue.getFloat();

			float keyWidth = tt9.getWidth() / 5f * functionKeyScale;
			return keyWidth * SettingsStore.SOFT_KEY_AMOUNT_OF_KEY_WIDTH_FOR_SWIPE;
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Error calculating the swipe X threshold. Using default to prevent crashing. " + e);
			return getSwipeYThreshold();
		}
	}


	private void updateSwipeTimingStats() {
		long time = Timer.get(LOG_TAG);

		long deltaT = time - swipeProcessingTimeAverage;
		if (deltaT < -swipeProcessingTimeAverage || deltaT > 5) {
			swipeCount = 0;
			swipeProcessingTime = 0;
		}

		swipeCount++;
		swipeProcessingTime += time;
		swipeProcessingTimeAverage = swipeProcessingTime / swipeCount;
	}


	protected long getAverageSwipeProcessingTime() {
		return swipeProcessingTimeAverage;
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				Timer.start(LOG_TAG);
				initSwipeThresholds();
				onPress(event);
				break;
			case MotionEvent.ACTION_MOVE:
				onMove(v, event);
				break;
			case MotionEvent.ACTION_UP:
				onRelease(event);
				break;
		}

		return super.onTouch(v, event);
	}


	@Override
	public boolean onLongClick(View view) {
		if (System.currentTimeMillis() - startTime < HOLD_DURATION_THRESHOLD) {
			return false;
		}

		isHolding = !isSwipingY && !isSwipingX;
		return !isHolding || super.onLongClick(view);
	}


	private void onPress(MotionEvent event) {
		startTime = System.currentTimeMillis();
		startX = event.getRawX();
		startY = event.getRawY();

		isHolding = false;
		isSwipingX = false;
		isSwipingY = false;
	}


	private void onMove(View v, MotionEvent event) {
		if (isHolding) {
			return;
		}

		float deltaY = event.getRawY() - startY;
		float deltaX = event.getRawX() - startX;

		if (isSwipingY) {
			handleSwipeY(event.getRawY(), deltaY);
		} else if (isSwipingX) {
			handleSwipeX(event.getRawX(), deltaX);
		} else if (Math.abs(deltaY) >= SWIPE_Y_THRESHOLD) {
			isSwipingY = true;
			updateSwipeTimingStats();
			handleStartSwipeY(event.getRawY(), deltaY);
		} else if (Math.abs(deltaX) >= SWIPE_X_THRESHOLD) {
			isSwipingX = true;
			updateSwipeTimingStats();
			handleStartSwipeX(event.getRawX(), deltaX);
		} else if (!isHolding && Math.abs(deltaX) < SWIPE_X_THRESHOLD && Math.abs(deltaY) < SWIPE_Y_THRESHOLD) {
			onLongClick(v);
		}
	}


	private void onRelease(MotionEvent event) {
		notSwiped = !isSwipingY && !isSwipingX;

		if (isSwipingY) {
			isSwipingY = false;
			handleEndSwipeY(event.getRawY(), event.getRawY() - startY);
		} else if (isSwipingX) {
			isSwipingX = false;
			handleEndSwipeX(event.getRawX(), event.getRawX() - startX);
		}
	}


	protected void handleStartSwipeX(float position, float delta) {}
	protected void handleStartSwipeY(float position, float delta) {}
	protected void handleSwipeX(float position, float delta) {}
	protected void handleSwipeY(float position, float delta) {}
	protected void handleEndSwipeX(float position, float delta) {}
	protected void handleEndSwipeY(float position, float delta) {}
	protected boolean notSwiped() { return notSwiped; }


	@Override
	public void render() {
		// readjust the action detection delays for keys that set them dynamically
		resetTimeThreshold();
		super.render();
	}
}
