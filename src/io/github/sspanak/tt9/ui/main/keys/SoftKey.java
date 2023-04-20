package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

public class SoftKey extends androidx.appcompat.widget.AppCompatButton implements View.OnTouchListener, View.OnLongClickListener {
	protected TraditionalT9 tt9;

	protected float COMPLEX_LABEL_TITLE_SIZE = 0.55f;
	protected float COMPLEX_LABEL_SUB_TITLE_SIZE = 0.8f;

	private boolean hold = false;
	private boolean repeat = false;
	private final Handler repeatHandler = new Handler(Looper.getMainLooper());


	public SoftKey(Context context) {
		super(context);
	}

	public SoftKey(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SoftKey(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public void setTT9(TraditionalT9 tt9) {
		this.tt9 = tt9;
	}

	public void setDarkTheme(boolean darkEnabled) {
		int textColor = ContextCompat.getColor(
			getContext(),
			darkEnabled ? R.color.dark_button_text : R.color.button_text
		);
		setTextColor(textColor);
	}


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		getRootView().setOnTouchListener(this);
		getRootView().setOnLongClickListener(this);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		super.onTouchEvent(event);

		int action = (event.getAction() & MotionEvent.ACTION_MASK);

		if (action == MotionEvent.ACTION_DOWN) {
			return handlePress();
		} else if (action == MotionEvent.ACTION_UP) {
			preventRepeat();
			if (!repeat) {
				return handleRelease();
			}
			repeat = false;
		}

		return false;
	}

	@Override
	public boolean onLongClick(View view) {
		hold = true;

		// sometimes this gets called twice, so we debounce the call to the repeating function
		repeatHandler.removeCallbacks(this::repeatOnLongPress);
		repeatHandler.postDelayed(this::repeatOnLongPress, 1);
		return true;
	}

	/**
	 * repeatOnLongPress
	 * Repeatedly calls "handleHold()" upon holding the respective SoftKey, to simulate physical keyboard behavior.
	 */
	private void repeatOnLongPress() {
		if (tt9 == null) {
			Logger.w(getClass().getCanonicalName(), "Traditional T9 handler is not set. Ignoring key press.");
			hold = false;
			return;
		}

		if (hold) {
			repeat = true;
			handleHold();
			repeatHandler.removeCallbacks(this::repeatOnLongPress);
			repeatHandler.postDelayed(this::repeatOnLongPress, tt9.getSettings().getSoftKeyRepeatDelay());
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

	protected boolean handlePress() {
		return false;
	}

	protected boolean handleHold() {
		return false;
	}

	protected boolean handleRelease() {
		if (tt9 == null) {
			Logger.w(getClass().getCanonicalName(), "Traditional T9 handler is not set. Ignoring key press.");
			return false;
		}

		int keyId = getId();

		if (keyId == R.id.soft_key_add_word) return tt9.onKeyAddWord();
		if (keyId == R.id.soft_key_input_mode) return tt9.onKeyNextInputMode();
		if (keyId == R.id.soft_key_language) return tt9.onKeyNextLanguage();
		if (keyId == R.id.soft_key_ok) return tt9.onOK();
		if (keyId == R.id.soft_key_settings) return tt9.onKeyShowSettings();

		return false;
	}

	/**
	 * getTitle
	 * Generates the name of the key, for example: "OK", "Backspace", "1", etc...
	 */
	protected String getTitle() {
		return null;
	}

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
	 * render
	 * Sets the key label using "getTitle()" and "getSubtitle()" or if they both
	 * return NULL, the XML "text" attribute will be preserved.
	 *
	 * If there is only name label, it will be centered and at normal font size.
	 * If there is also a function label, it will be displayed below the name label and both will
	 * have their font size adjusted to fit inside the key.
	 */
	public void render() {
		String title = getTitle();
		String subtitle = getSubTitle();

		if (title == null) {
			return;
		} else if (subtitle == null) {
			setText(title);
			return;
		}

		SpannableStringBuilder sb = new SpannableStringBuilder(title);
		sb.append('\n');
		sb.append(subtitle);

		sb.setSpan(new RelativeSizeSpan(COMPLEX_LABEL_TITLE_SIZE), 0, 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		sb.setSpan(new StyleSpan(Typeface.ITALIC), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		sb.setSpan(new RelativeSizeSpan(COMPLEX_LABEL_SUB_TITLE_SIZE), 1, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

		setText(sb);
	}
}
