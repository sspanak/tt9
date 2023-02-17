package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class SoftBackspaceKey extends SoftKey {
	private SettingsStore settings;
	long lastBackspaceCall = 0;

	public SoftBackspaceKey(Context context) {
		super(context);
	}

	public SoftBackspaceKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftBackspaceKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private SettingsStore getSettings() {
		if (settings == null) {
			settings = new SettingsStore(getContext());
		}

		return settings;
	}

	@Override
	final public boolean onTouch(View view, MotionEvent event) {
		if (tt9 == null) {
			Logger.w(getClass().getCanonicalName(), "Traditional T9 handler is not set. Ignoring key press.");
			return false;
		}

		int action = event.getAction() & MotionEvent.ACTION_MASK;

		if (action == MotionEvent.AXIS_PRESSURE) {
			handleHold();
		} else if (action == MotionEvent.ACTION_UP) {
			handleUp();
		} else if (action == MotionEvent.ACTION_DOWN) {
			// Fallback for phones that do not report AXIS_PRESSURE, when a key is being held
			handlePress(-1);
		}

		return true;
	}

	private void handleHold() {
		if (System.currentTimeMillis() - lastBackspaceCall < getSettings().getSoftKeyRepeatDelay()) {
			return;
		}

		handlePress(-1);

		long now = System.currentTimeMillis();
		lastBackspaceCall = lastBackspaceCall == 0 ? getSettings().getSoftKeyInitialDelay() + now : now;
	}

	private void handleUp() {
		lastBackspaceCall = 0;
	}

	@Override
	final protected boolean handlePress(int b) {
		return tt9.onBackspace();
	}
}
