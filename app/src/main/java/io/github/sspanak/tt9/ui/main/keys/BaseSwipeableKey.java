package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Timer;

abstract public class BaseSwipeableKey extends BaseSoftKeyWithSideText {
	private final String TIMER_ID = BaseSwipeableKey.class.getSimpleName() + System.identityHashCode(this);

	private float HOLD_DURATION_THRESHOLD;
	protected float SWIPE_X_THRESHOLD;
	protected float SWIPE_Y_THRESHOLD;

	private boolean isHolding = false;
	private boolean isSwipingX = false;
	private boolean isSwipingY = false;
	private boolean notSwiped = true;
	protected boolean isSwipeable = false;

	private float startX;
	private float startY;

	private int swipeCount = 0;
	private long swipeProcessingTime = 0;
	private long swipeProcessingTimeAverage = 50;

	private final Handler longClickWaitHandler = new Handler(Looper.getMainLooper());


	public BaseSwipeableKey(Context context) {
		super(context);
	}


	public BaseSwipeableKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public BaseSwipeableKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	@Override
	protected void onDetachedFromWindow() {
		cancelLongClick();
		super.onDetachedFromWindow();
	}


	private void initTimeThreshold() {
		if (HOLD_DURATION_THRESHOLD == 0) {
			HOLD_DURATION_THRESHOLD = getHoldDurationThreshold();
		}
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

	protected void resetSwipeThresholds() {
		SWIPE_X_THRESHOLD = 0;
		SWIPE_Y_THRESHOLD = 0;
		initSwipeThresholds();
	}


	protected float getHoldDurationThreshold() { return ViewConfiguration.getLongPressTimeout(); }
	protected float getSwipeYThreshold() { return getResources().getDimensionPixelSize(R.dimen.numpad_key_height) * SettingsStore.SOFT_KEY_AMOUNT_OF_KEY_SIZE_FOR_SWIPE; }


	/**
	 * Calculate the minimum amount of finger movement to be considered a swipe. It is meant
	 * to prevent accidental swipes when pressing or holding the key.
	 */
	protected float getSwipeXThreshold() {
		// If the key width is not available, use the old method. It's better than nothing.
		final int widthPx = getWidth();
		if (tt9 == null || widthPx == 0) {
			return getSwipeYThreshold();
		}

		float keyWidth = widthPx * tt9.getSettings().getNumpadFnKeyDefaultScale();
		return keyWidth * SettingsStore.SOFT_KEY_AMOUNT_OF_KEY_SIZE_FOR_SWIPE;
	}


	private void updateSwipeTimingStats() {
		long time = Timer.get(TIMER_ID);

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
		if (isSwipeable) {
			setOnLongClickListener(null);
		} else {
			setOnLongClickListener(this);
			return super.onTouch(v, event);
		}

		switch(event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				initTimeThreshold();
				initSwipeThresholds();
				onPress(event);
				break;
			case MotionEvent.ACTION_MOVE:
				onMove(event);
				break;
			case MotionEvent.ACTION_UP:
				onRelease(event);
				break;
		}

		return super.onTouch(v, event);
	}


	@Override
	public boolean onLongClick(View view) {
		if (!isSwipeable) {
			return super.onLongClick(view);
		}

		isHolding = !isSwipingY && !isSwipingX;
		return !isHolding || super.onLongClick(view);
	}


	private void onPress(MotionEvent event) {
		Timer.start(TIMER_ID);

		startX = event.getRawX();
		startY = event.getRawY();

		isHolding = false;
		isSwipingX = false;
		isSwipingY = false;

		if (HOLD_DURATION_THRESHOLD < Float.MAX_VALUE) {
			cancelLongClick();
			longClickWaitHandler.postDelayed(() -> onLongClick(this), Math.round(HOLD_DURATION_THRESHOLD));
		}
	}


	private void onMove(MotionEvent event) {
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
			cancelLongClick();
			updateSwipeTimingStats();
			handleStartSwipeY(event.getRawY(), deltaY);
		} else if (Math.abs(deltaX) >= SWIPE_X_THRESHOLD) {
			isSwipingX = true;
			cancelLongClick();
			updateSwipeTimingStats();
			handleStartSwipeX(event.getRawX(), deltaX);
		}
	}


	private void onRelease(MotionEvent event) {
		cancelLongClick();
		Timer.stop(TIMER_ID);

		notSwiped = !isSwipingY && !isSwipingX;

		if (isSwipingY) {
			isSwipingY = false;
			handleEndSwipeY(event.getRawY(), event.getRawY() - startY);
		} else if (isSwipingX) {
			isSwipingX = false;
			handleEndSwipeX(event.getRawX(), event.getRawX() - startX);
		}
	}


	private void cancelLongClick() {
		longClickWaitHandler.removeCallbacksAndMessages(null);
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
		if (isSwipeable) {
			// readjust the action detection delays for keys that set them dynamically
			resetTimeThreshold();
		}
		super.render();
	}
}
