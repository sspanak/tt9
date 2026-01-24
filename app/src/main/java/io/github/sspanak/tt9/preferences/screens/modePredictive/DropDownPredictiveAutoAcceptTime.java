package io.github.sspanak.tt9.preferences.screens.modePredictive;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.screens.modeAbc.DropDownAbcAutoAcceptTime;

public class DropDownPredictiveAutoAcceptTime extends DropDownAbcAutoAcceptTime {
	public static final String NAME = "pref_predictive_auto_accept_time";
	public static final int DEFAULT = -1;

	public DropDownPredictiveAutoAcceptTime(@NonNull Context context) { super(context); }
	public DropDownPredictiveAutoAcceptTime(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownPredictiveAutoAcceptTime(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownPredictiveAutoAcceptTime(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override protected String getName() { return NAME; }

	@Override
	public String getStringifiedDefault() {
		return String.valueOf(DEFAULT);
	}
}
