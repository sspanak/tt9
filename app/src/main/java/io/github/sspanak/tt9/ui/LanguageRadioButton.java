package io.github.sspanak.tt9.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;

public class LanguageRadioButton extends LinearLayout {
	private static Drawable highlightDrawable;

	private TextView label;
	private RadioButton radio;

	public LanguageRadioButton(Context context) { super(context); init(context); }
	public LanguageRadioButton(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public LanguageRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }

	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.radio_button_language, this, true);
		setOrientation(HORIZONTAL);
		label = findViewById(R.id.radio_label);
		radio = findViewById(R.id.radio_button);
		if (highlightDrawable == null) {
			highlightDrawable = AppCompatResources.getDrawable(context, R.color.key_num_ripple);
		}
	}

	/**
	 * autoHighlightCompat
	 * Used to highlight the button when scrolling with the DPAD. Normally, this is done by setting
	 * android:background="?android:attr/selectableItemBackground" in the styles. However, this does
	 * not work on Sonim XP3800, which means it may not work on other devices as well. So... here is
	 * one more device hack.
	 */
	public void autoHighlightCompat() {
		setBackground(hasFocus() ? highlightDrawable : null);
	}

	public LanguageRadioButton setLanguage(@NonNull Language language, String labelPrefix) {
		setId(language.getId());
		radio.setId(language.getId());

		final String text = labelPrefix != null ? labelPrefix + language.getName() : language.getName();
		label.setText(text);

		return this;
	}

	public LanguageRadioButton setChecked(boolean checked) {
		radio.setChecked(checked);
		return this;
	}

	/**
	 * On Android 5, ensure there is no clickable="true" in the XML, otherwise the
	 * listener will not be called.
	 */
	public LanguageRadioButton setOnClick(@Nullable OnClickListener l) {
		super.setOnClickListener(l);
		return this;
	}
}
