package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;

public class SoftKey extends com.google.android.material.button.MaterialButton implements View.OnTouchListener, View.OnLongClickListener {
	private final String LOG_TAG = getClass().getSimpleName();

	protected TraditionalT9 tt9;
	protected Vibration vibration;

	private boolean hold = false;
	private boolean repeat = false;
	private long lastLongClickTime = 0;
	private final Handler repeatHandler = new Handler(Looper.getMainLooper());

	private static int lastPressedKey = -1;
	private boolean ignoreLastPressedKey = false;

	private boolean isTitleDisabled = false;


	public SoftKey(Context context) {
		super(context);
		setHapticFeedbackEnabled(false);
		setOnTouchListener(this);
		setOnLongClickListener(this);
	}

	public SoftKey(Context context, AttributeSet attrs) {
		super(context, attrs);
		setHapticFeedbackEnabled(false);
		setOnTouchListener(this);
		setOnLongClickListener(this);
	}

	public SoftKey(Context context, AttributeSet attrs, int defStyleAttr) {
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


	public void setDarkTheme(boolean darkEnabled) {
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setTextColor(getTextColors().withAlpha(enabled ? 255 : 80));
	}


	/**
	 * getTitle
	 * Generates the name of the key, for example: "OK", "Backspace", "1", etc...
	 */
	protected String getTitle() {
		return null;
	}


	/**
	 * getNoEmojiTitle
	 * Generates a text representation of the key title, when emojis are not supported and getTitle()
	 * is meant to return an emoji.
	 */
	protected int getNoEmojiTitle() { return 0; }


	/**
	 * getSubTitle
	 * Generates a String describing what the key does.
	 * For example: "ABC" for 2-key; "⌫" for Backspace key, "⚙" for Settings key, and so on.
	 *
	 * The sub title label is optional.
	 */
	protected String getSubTitle() {
		return null;
	}


	/**
	 * Returns a meaningful key title depending on the current emoji support.
	 */
	private String getTitleCompat() {
		if (
			getNoEmojiTitle() > 0
			&& (
				Characters.noEmojiSupported()
				|| (new Text(getText().toString()).startsWithGraphic() && !new Paint().hasGlyph(getText().toString()))
			)
		) {
			return getContext().getString(getNoEmojiTitle());
		} else {
			return getTitle();
		}
	}


	/**
	 * Multiplier for the font size when there is only one label.
	 */
	protected float getSingleLabelRelativeSize() {
		return 1;
	}


	/**
	 * Multiplier for the title font size when there are two labels.
	 */
	protected float getTitleRelativeSize() {
		return SettingsStore.SOFT_KEY_COMPLEX_LABEL_TITLE_RELATIVE_SIZE;
	}


	/**
	 * Multiplier for the subtitle font size when there are two labels.
	 */
	protected float getSubTitleRelativeSize() {
		return SettingsStore.SOFT_KEY_COMPLEX_LABEL_SUB_TITLE_RELATIVE_SIZE;
	}



	/**
	 * render
	 * Sets the key label using "getTitle()" and "getSubtitle()" or if they both
	 * return NULL, the XML "text" attribute will be preserved.
	 *
	 * If there is only name label, it will be centered and at normal font size.
	 * If there is also a function label, it will be displayed below the name label and both will
	 * have their font size adjusted to fit inside the key.
	 */
	public void render() {
		String title = getTitleCompat();
		String subtitle = getSubTitle();

		if (title == null) {
			return;
		}

		SpannableStringBuilder sb = new SpannableStringBuilder(title);
		if (subtitle != null) {
			sb.append(" ");
			sb.append(subtitle);
		}

		setText(sb);
	}


	protected void setTitleDisabled(boolean yes) {
		isTitleDisabled = yes;
	}


	protected void vibrate(int vibrationType) {
		if (tt9 != null) {
			vibration = vibration == null ? new Vibration(tt9.getSettings(), this) : vibration;
			vibration.vibrate(vibrationType);
		}
	}
}
