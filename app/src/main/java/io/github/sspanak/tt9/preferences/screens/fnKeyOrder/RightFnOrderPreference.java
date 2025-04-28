package io.github.sspanak.tt9.preferences.screens.fnKeyOrder;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RightFnOrderPreference extends LeftFnOrderPreference {
	public final static String NAME = "pref_rfn_key_order";

	public RightFnOrderPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public RightFnOrderPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public RightFnOrderPreference(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public RightFnOrderPreference(@NonNull Context context) { super(context); }

	@Override
	protected void populate() {
		setText(settings.getRfnKeyOrder());
	}
}
