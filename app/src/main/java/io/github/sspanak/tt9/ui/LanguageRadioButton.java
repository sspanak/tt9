package io.github.sspanak.tt9.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;

public class LanguageRadioButton extends LinearLayout {
	private TextView label;
	private RadioButton radio;

	public LanguageRadioButton(Context context) { super(context); init(context); }
	public LanguageRadioButton(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public LanguageRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }
	public LanguageRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(context); }

	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.radio_button_language, this, true);
		setOrientation(HORIZONTAL);
		label = findViewById(R.id.radio_label);
		radio = findViewById(R.id.radio_button);
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
