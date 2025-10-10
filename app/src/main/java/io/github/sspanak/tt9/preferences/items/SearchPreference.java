package io.github.sspanak.tt9.preferences.items;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

abstract public class SearchPreference extends TextInputPreference {
	public SearchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	public SearchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public SearchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	public SearchPreference(@NonNull Context context) {
		super(context);
	}


	@Override protected int getLargeLayout() {
		return DeviceInfo.AT_LEAST_ANDROID_12 ? R.layout.pref_input_text : R.layout.pref_input_text_large;
	}

	@Override
	protected int getIconResource() {
		return R.drawable.ic_fn_search;
	}
}
