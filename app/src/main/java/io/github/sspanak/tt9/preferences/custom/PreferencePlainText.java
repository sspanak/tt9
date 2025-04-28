package io.github.sspanak.tt9.preferences.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;

public class PreferencePlainText extends ScreenPreference {
	public PreferencePlainText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferencePlainText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferencePlainText(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferencePlainText(@NonNull Context context) { super(context); }

	@Override
	public int getDefaultLayout() {
		return R.layout.pref_plain_text;
	}

	@Override
	public int getLargeLayout() {
		return R.layout.pref_plain_text_large;
	}
}
