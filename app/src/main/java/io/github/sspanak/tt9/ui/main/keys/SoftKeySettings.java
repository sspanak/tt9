package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ui.main.ResizableMainView;

public class SoftKeySettings extends BaseSwipeableKey {
	private ResizableMainView mainView;

	public SoftKeySettings(Context context) {
		super(context);
	}
	public SoftKeySettings(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public SoftKeySettings(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setMainView(ResizableMainView mainView) {
		this.mainView = mainView;
	}

	// this key does not support holding at the moment, so just prevent it
	@Override protected float getHoldDurationThreshold() { return 1000; }
	@Override protected float getSwipeXThreshold() { return getResources().getDimensionPixelSize(R.dimen.numpad_key_height) * 0.75f; }
	@Override protected float getSwipeYThreshold() { return getResources().getDimensionPixelSize(R.dimen.numpad_key_height) / 4.0f; }

	@Override
	protected boolean handleRelease() {
		if (notSwiped() && validateTT9Handler()) {
			tt9.showSettings();
		}
		return true;
	}

	@Override
	protected void handleStartSwipeX(float p, float delta) {
		if (mainView != null) mainView.onAlign(delta);
	}

	@Override
	protected void handleStartSwipeY(float position, float d) {
		if (mainView != null) mainView.onResizeStart(position);
	}

	@Override
	protected void handleSwipeY(float position, float delta) {
		if (mainView != null) mainView.onResizeThrottled(position);
	}

	@Override
	protected void handleEndSwipeY(float position, float delta) {
		if (mainView != null) mainView.onResize(position);
	}


	@Override
	protected int getCentralIcon() {
		return R.drawable.ic_fn_settings;
	}

	@Override
	protected String getTopText() {
		return getContext().getString(R.string.key_dpad_up);
	}

	@Override
	protected String getRightText() {
		return getContext().getString(R.string.key_dpad_right);
	}

	@Override
	protected String getBottomText() {
		return getContext().getString(R.string.key_dpad_down);
	}

	@Override
	protected String getLeftText() {
		return getContext().getString(R.string.key_dpad_left);
	}

	@Override
	protected float getCentralIconScale() {
		return super.getCentralIconScale() * 0.9f;
	}
}
