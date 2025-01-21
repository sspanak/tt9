package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

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

	private Drawable icon = null;
	private Drawable holdIcon = null;
	private RelativeLayout overlay = null;


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


	protected float getTT9Width() {
		return tt9 != null ? tt9.getSettings().getNumpadWidthPercent() / 100f : 1;
	}


	protected float getTT9Height() {
		return tt9 != null ? (float) tt9.getSettings().getNumpadKeyHeight() / (float) tt9.getSettings().getNumpadKeyDefaultHeight() : 1;
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


	protected int getCentralIcon() {
		return -1;
	}


	protected int getHoldIcon() {
		return -1;
	}


	protected void resetIconCache() {
		icon = null;
		holdIcon = null;
	}


	/**
	 * Generates a String describing the "hold" function of the key. The String will be displayed
	 * in the upper right corner.
	 */
	protected String getHoldText() {
		return null;
	}


	/**
	 * Generates a String describing for the swipe up function of the key
	 */
	protected String getTopText() {
		return null;
	}

	/**
	 * Generates a String describing for the swipe right function of the key
	 */
	protected String getRightText() {
		return null;
	}

	/**
	 * Generates a String describing for the swipe down function of the key
	 */
	protected String getBottomText() {
		return null;
	}

	/**
	 * Generates a String describing for the swipe left function of the key
	 */
	protected String getLeftText() {
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
	 * A fail-safe method to get the central icon drawable.
	 */
	private Drawable getIconCompat() {
		if (icon == null && getCentralIcon() > 0) {
			icon = AppCompatResources.getDrawable(getContext(), getCentralIcon());
		}

		return icon;
	}


	/**
	 * A fail-safe method to get the hold icon drawable.
	 */
	private Drawable getHoldIconCompat() {
		if (holdIcon == null && getHoldIcon() > 0) {
			holdIcon = AppCompatResources.getDrawable(getContext(), getHoldIcon());
		}

		return holdIcon;
	}


	/**
	 * Multiplier for the main text font size. Used for automatically adjusting the font size to fit
	 * the key when changing the keyboard dimensions.
	 */
	protected float getTitleScale() {
		return SettingsStore.SOFT_KEY_CONTENT_DEFAULT_SCALE * Math.min(getTT9Width(), getTT9Height());
	}


	/**
	 * Same as getTitleScale(), but for keys that have icons instead of text.
	 */
	protected float getCentralIconScale() {
		float width = getTT9Width();
		return width > 0.95f ? Math.min(1.15f, getTT9Height()) : Math.min(width, getTT9Height());
	}


	/**
	 * Similar to getTitleScale(), adjusts the font size of the hold text or icon
	 */
	protected float getHoldElementScale() {
		return SettingsStore.SOFT_KEY_CONTENT_DEFAULT_SCALE * Math.min(1, getTT9Height());
	}


	private void getOverlayWrapper() {
		if (overlay == null) {
			ViewParent parent = getParent();
			if (parent instanceof RelativeLayout) {
				overlay = (RelativeLayout) parent;
			}
		}
	}


	/**
	 * render
	 * Sets the key labels and icons using "getTitle()", "getCenterIcon()", "getHoldText()",
	 * "getTopText()", "getRightText()", "getHoldIcon()", etc. Also takes care of styling the labels
	 * depending on "isEnabled()" and "isHoldEnabled()".
	 */
	public void render() {
		boolean isKeyEnabled = isEnabled();
		boolean isHoldEnabled = isHoldEnabled();

		renderTitle(isKeyEnabled);

		getOverlayWrapper();

		renderOverlayDrawable("overlay_icon", getIconCompat(), getCentralIconScale(), isKeyEnabled);

		renderOverlayText("overlay_hold_text", getHoldText(), getHoldElementScale(), isKeyEnabled && isHoldEnabled);
		renderOverlayDrawable("overlay_hold_icon", getHoldIconCompat(), getHoldElementScale(), isKeyEnabled && isHoldEnabled);

		renderOverlayText("overlay_top_text", getTopText(), getHoldElementScale(), isKeyEnabled);
		renderOverlayText("overlay_right_text", getRightText(), getHoldElementScale(), isKeyEnabled);
		renderOverlayText("overlay_bottom_text", getBottomText(), getHoldElementScale(), isKeyEnabled);
		renderOverlayText("overlay_left_text", getLeftText(), getHoldElementScale(), isKeyEnabled);
	}


	/**
	 * Renders the central text of the key and styles it based on "isEnabled".
	 */
	private void renderTitle(boolean isEnabled) {
		String title = getTitleCompat();
		if (title == null) {
			return;
		}

		setTextColor(getTextColors().withAlpha(isEnabled ? 255 : 110));

		float scale = getTitleScale();
		if (scale == 1) {
			setText(title);
			return;
		}

		SpannableString text = new SpannableString(title);
		text.setSpan(new RelativeSizeSpan(scale), 0, title.length(), 0);
		setText(text);
	}


	/**
	 * Renders text in the given overlay element, with optional scaling and alpha. The overlay
	 * text elements are either the "hold" text or the "swipe" text.
	 */
	private void renderOverlayText(String elementTag, @Nullable String text, float scale, boolean isEnabled) {
		if (overlay == null) {
			return;
		}

		View element = ((RelativeLayout) getParent()).findViewWithTag(elementTag);
		if (!(element instanceof TextView el)) {
			return;
		}

		el.setTextColor(el.getTextColors().withAlpha(isEnabled ? 255 : 110));

		if (text == null || scale == 1) {
			el.setText(text);
			return;
		}

		SpannableString scaledText = new SpannableString(text);
		scaledText.setSpan(new RelativeSizeSpan(scale), 0, scaledText.length(), 0);
		el.setText(scaledText);
	}


	/**
	 * Renders one of the key icons. It could be either the central icon, in the place of the main title,
	 * or a hold icon, displayed in the upper right corner.
	 */
	private void renderOverlayDrawable(String elementTag, @Nullable Drawable drawable, float scale, boolean isEnabled) {
		if (overlay == null) {
			return;
		}

		View element = ((RelativeLayout) getParent()).findViewWithTag(elementTag);
		if (!(element instanceof ImageView el)) {
			return;
		}

		el.setImageDrawable(drawable);
		if (!isEnabled) {
			el.setColorFilter(Color.GRAY);
		} else {
			el.clearColorFilter();
		}

		if (drawable != null) {
			el.setScaleX(scale);
			el.setScaleY(scale);
		}
	}


	protected void vibrate(int vibrationType) {
		if (tt9 != null) {
			vibration = vibration == null ? new Vibration(tt9.getSettings(), this) : vibration;
			vibration.vibrate(vibrationType);
		}
	}
}
