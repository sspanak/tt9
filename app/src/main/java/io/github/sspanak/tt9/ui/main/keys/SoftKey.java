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

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class SoftKey extends androidx.appcompat.widget.AppCompatButton implements View.OnTouchListener, View.OnLongClickListener {
	protected TraditionalT9 tt9;

	protected float complexLabelTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_TITLE_SIZE;
	protected float complexLabelSubTitleSize = SettingsStore.SOFT_KEY_COMPLEX_LABEL_SUB_TITLE_SIZE;

	private boolean hold = false;
	private boolean repeat = false;
	private final Handler repeatHandler = new Handler(Looper.getMainLooper());
	private static int lastPressedKey = -1;


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
				boolean result = handleRelease();
				lastPressedKey = getId();
				return result;
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
		if (!validateTT9Handler()) {
			hold = false;
			return;
		}

		if (hold) {
			repeat = true;
			handleHold();
			lastPressedKey = getId();
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

	protected boolean handlePress() {
		return false;
	}

	protected boolean handleHold() {
		return false;
	}

	protected boolean handleRelease() {
		if (!validateTT9Handler()) {
			return false;
		}

		int keyId = getId();
		boolean multiplePress = lastPressedKey == keyId;

		if (keyId == R.id.soft_key_add_word) return tt9.onKeyAddWord(false);
		if (keyId == R.id.soft_key_filter_suggestions) return tt9.onKeyFilterSuggestions(false, multiplePress);
		if (keyId == R.id.soft_key_clear_filter) return tt9.onKeyFilterClear(false);
		if (keyId == R.id.soft_key_left_arrow) return tt9.onKeyScrollSuggestion(false, true);
		if (keyId == R.id.soft_key_right_arrow) return tt9.onKeyScrollSuggestion(false, false);
		if (keyId == R.id.soft_key_language) return tt9.onKeyNextLanguage(false);
		if (keyId == R.id.soft_key_settings) return tt9.onKeyShowSettings(false);

		return false;
	}

	protected boolean validateTT9Handler() {
		if (tt9 == null) {
			Logger.w(getClass().getCanonicalName(), "Traditional T9 handler is not set. Ignoring key press.");
			return false;
		}

		return true;
	}

	@Nullable protected Language getCurrentLanguage() {
		return LanguageCollection.getLanguage(tt9.getApplicationContext(), tt9.getSettings().getInputLanguage());
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

		float padding = SettingsStore.SOFT_KEY_COMPLEX_LABEL_TITLE_SIZE;
		if (complexLabelTitleSize == SettingsStore.SOFT_KEY_COMPLEX_LABEL_ARABIC_TITLE_SIZE) {
			padding /= 10;
		}

		sb.setSpan(new RelativeSizeSpan(complexLabelTitleSize), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		sb.setSpan(new StyleSpan(Typeface.ITALIC), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		sb.setSpan(new RelativeSizeSpan(padding), 1, 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		sb.setSpan(new RelativeSizeSpan(complexLabelSubTitleSize), 2, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

		setText(sb);
	}
}
