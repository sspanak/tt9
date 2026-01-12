package io.github.sspanak.tt9.preferences.screens.keypad;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.sys.HardwareInfo;

public class SwitchUpsideDownKeys extends SwitchPreferenceCompat {
	public static final String NAME = "pref_upside_down_keys";
	public static final boolean DEFAULT = HardwareInfo.IS_EMULATOR;

	public SwitchUpsideDownKeys(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }
	public SwitchUpsideDownKeys(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
	public SwitchUpsideDownKeys(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
	public SwitchUpsideDownKeys(@NonNull Context context) { super(context); init(); }

	private void init() {
		setKey(NAME);
		setTitle(R.string.pref_upside_down_keys);
		setSummary(R.string.pref_upside_down_keys_summary);
		setDefaultValue(HardwareInfo.IS_EMULATOR);
		setChecked(HardwareInfo.IS_EMULATOR);
	}
}
