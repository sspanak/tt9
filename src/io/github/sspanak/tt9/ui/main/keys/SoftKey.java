package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.graphics.Typeface;
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

public class SoftKey extends androidx.appcompat.widget.AppCompatButton implements View.OnTouchListener {
	protected TraditionalT9 tt9;

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
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		super.onTouchEvent(event);

		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
			return handlePress(view.getId());
		}

		return false;
	}

	protected boolean handlePress(int keyId) {
		if (tt9 == null) {
			Logger.w(getClass().getCanonicalName(), "Traditional T9 handler is not set. Ignoring key press.");
			return false;
		}

		if (keyId == R.id.soft_key_add_word) return tt9.onKeyAddWord();
		if (keyId == R.id.soft_key_input_mode) return tt9.onKeyNextInputMode();
		if (keyId == R.id.soft_key_language) return tt9.onKeyNextLanguage();
		if (keyId == R.id.soft_key_ok) return tt9.onOK();
		if (keyId == R.id.soft_key_settings) return tt9.onKeyShowSettings();

		return false;
	}

	/**
	 * Generates the name of the key, for example: "OK", "Backspace", "1", etc...
	 */
	protected String getKeyNameLabel() {
		return null;
	}

	/**
	 * Generates a String describing what the key does.
	 * For example: "ABC" for 2-key; "⌫" for Backspace key, "⚙" for Settings key, and so on.
	 *
	 * The function label is optional.
	 */
	protected String getKeyFunctionLabel() {
		return null;
	}

	/**
	 * render
	 * Sets the key label using "getKeyNameLabel()" and "getKeyFunctionLabel()" or if they both
	 * return NULL, the XML "text" attribute will be preserved.
	 *
	 * If there is only name label, it will be centered and at normal font size.
	 * If there is also a function label, it will be displayed below the name label and both will
	 * have their font size adjusted to fit inside the key.
	 */
	public void render() {
		String name = getKeyNameLabel();
		String func = getKeyFunctionLabel();

		if (name == null) {
			return;
		} else if (func == null) {
			setText(name);
			return;
		}

		SpannableStringBuilder sb = new SpannableStringBuilder(name);
		sb.append('\n');
		sb.append(func);

		sb.setSpan(new RelativeSizeSpan(0.55f), 0, 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		sb.setSpan(new StyleSpan(Typeface.ITALIC), 0, 2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		sb.setSpan(new RelativeSizeSpan(0.75f), 1, sb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

		setText(sb);
	}
}
