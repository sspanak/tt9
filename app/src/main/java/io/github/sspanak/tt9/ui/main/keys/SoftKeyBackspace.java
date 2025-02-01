package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;

public class SoftKeyBackspace extends BaseSwipeableKey {
	private int repeat = 0;

	private boolean isActionPerformed = false;
	private final Handler waitForSwipe = new Handler();


	public SoftKeyBackspace(Context context) { super(context); }
	public SoftKeyBackspace(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyBackspace(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private boolean isFastDeleteOn() {
		return tt9 != null && tt9.getSettings().getBackspaceAcceleration();
	}


	/**
	 * When fast-delete is on, decrease the hold duration threshold for smoother operation.
	 */
	@Override
	protected float getHoldDurationThreshold() {
		return isFastDeleteOn() ? SettingsStore.SOFT_KEY_REPEAT_DELAY * 3 : super.getHoldDurationThreshold();
	}


	/**
	 * When fast-delete is on, prevent swapping that does nothing. It may feel frustrating if the user
	 * moves their finger slightly and the key does not delete anything.
	 */
	@Override
	protected float getSwipeXThreshold() {
		return isFastDeleteOn() ? super.getSwipeXThreshold() : Integer.MAX_VALUE;
	}


	/**
	 * Disable vertical swiping for backspace key.
	 */
	@Override
	protected float getSwipeYThreshold() {
		return Integer.MAX_VALUE;
	}


	@Override
	final protected boolean handlePress() {
		super.handlePress();
		isActionPerformed = false;
		waitForSwipe.postDelayed(this::handlePressDebounced, 1 + getAverageSwipeProcessingTime());
		return true;
	}


	/**
	 * Avoids deleting text twice when swiping - first, when the user touches the screen, and then,
	 * when they finish the swipe gesture.
	 */
	private void handlePressDebounced() {
		if (!isActionPerformed) {
			isActionPerformed = true;
			deleteText();
		}
	}


	@Override
	protected void handleStartSwipeX(float position, float delta) {
		if (!isActionPerformed) {
			isActionPerformed = true;
			deleteWord();
		}
	}


	@Override
	protected void handleSwipeX(float position, float delta) {
		handleStartSwipeX(position, delta);
	}


	@Override
	protected void handleEndSwipeX(float position, float delta) {
		if (isActionPerformed) {
			return;
		}

		isActionPerformed = true;

		if (delta < SWIPE_X_THRESHOLD) {
			deleteText();
		} else {
			deleteWord();
		}
	}


	@Override
	final protected void handleHold() {
		isActionPerformed = true;
		repeat++;
		deleteText();
	}


	@Override
	final protected boolean handleRelease() {
		vibrate(repeat > 0 ? Vibration.getReleaseVibration() : Vibration.getNoVibration());
		repeat = 0;

		return true;
	}


	private void deleteText() {
		if (validateTT9Handler() && !tt9.onBackspace(repeat)) {
			// Limited or special numeric field (e.g. formatted money or dates) cannot always return
			// the text length, therefore onBackspace() seems them as empty and does nothing. This results
			// in fallback to the default hardware key action. Here we simulate the hardware BACKSPACE.
			tt9.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
		}
	}


	private void deleteWord() {
		if (validateTT9Handler()) {
			tt9.onBackspace(SettingsStore.BACKSPACE_ACCELERATION_REPEAT_DEBOUNCE);
		}
	}


	@Override
	protected float getTitleScale() {
		float scale = tt9 != null && tt9.getSettings().isMainLayoutNumpad() ? super.getTitleScale() : 1;
		return scale * 1.1f;
	}


	@Override
	protected int getNoEmojiTitle() {
		return R.string.virtual_key_del;
	}


	@Override
	protected String getTitle() {
		return LanguageKind.isRTL(tt9 != null ? tt9.getLanguage() : null) ? "⌦" : "⌫";
	}


	@Override
	public void render() {
		super.render();
		if (tt9 != null) {
			setEnabled(!tt9.isVoiceInputActive());
		}
	}
}
