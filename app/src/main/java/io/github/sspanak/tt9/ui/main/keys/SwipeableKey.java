package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract public class SwipeableKey extends SoftKey {
	private float HOLD_DURATION_THRESHOLD;
	protected float SWIPE_X_THRESHOLD;
	protected float SWIPE_Y_THRESHOLD;

	private boolean isHolding = false;
	private boolean isSwipingX = false;
	private boolean isSwipingY = false;
	private boolean hasSwiped = false;

	private float startX;
	private float startY;
	private long startTime;


	public SwipeableKey(Context context) {
		super(context);
		init(context);
	}


	public SwipeableKey(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	public SwipeableKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}


	private void init(Context context) {
		if (HOLD_DURATION_THRESHOLD == 0) {
			HOLD_DURATION_THRESHOLD = getHoldDurationThreshold();
		}
		if (SWIPE_X_THRESHOLD == 0) {
			SWIPE_X_THRESHOLD = getSwipeXThreshold(context);
		}
		if (SWIPE_Y_THRESHOLD == 0) {
			SWIPE_Y_THRESHOLD = getSwipeYThreshold(context);
		}
	}


	protected float getHoldDurationThreshold() { return SettingsStore.SOFT_KEY_REPEAT_DELAY * 3; }
	protected float getSwipeXThreshold(Context context) { return context.getResources().getDimensionPixelSize(R.dimen.numpad_key_height) / 10.0f; }
	protected float getSwipeYThreshold(Context context) { return context.getResources().getDimensionPixelSize(R.dimen.numpad_key_height) / 10.0f; }


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
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
			return true;
		}

		isHolding = !isSwipingY && !isSwipingX;
		return isSwipingY || isSwipingX || super.onLongClick(view);
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
			handleStartSwipeY(event.getRawY(), deltaY);
		} else if (Math.abs(deltaX) >= SWIPE_X_THRESHOLD) {
			isSwipingX = true;
			handleStartSwipeX(event.getRawX(), deltaX);
		} else if (!isHolding && Math.abs(deltaX) < SWIPE_X_THRESHOLD && Math.abs(deltaY) < SWIPE_Y_THRESHOLD) {
			onLongClick(v);
		}
	}


	private void onRelease(MotionEvent event) {
		hasSwiped = !isSwipingY && !isSwipingX;

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
	protected boolean notSwiped() { return hasSwiped; }
}
