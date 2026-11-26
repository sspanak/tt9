package io.github.sspanak.tt9.preferences.screens.appearance;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.sys.HardwareInfo;

public class SwitchDoubleTapResize extends SwitchWhenVisibleLayout {
	public final static String NAME = "pref_double_tap_resize";
	public final static boolean DEFAULT = HardwareInfo.IS_EMULATOR;

	public SwitchDoubleTapResize(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }
	public SwitchDoubleTapResize(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
	public SwitchDoubleTapResize(android.content.Context context, android.util.AttributeSet attrs) { super(context, attrs); init(); }
	public SwitchDoubleTapResize(android.content.Context context) { super(context); init(); }

	private void init() {
		setKey(NAME);
		setDefaultValue(DEFAULT);
		setTitle(R.string.pref_double_tap_resize);
		setSummary(R.string.pref_double_tap_resize_summary);
		setChecked(getPersistedBoolean(DEFAULT));
	}
}
