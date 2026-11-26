package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SwitchDragResize extends SwitchWhenVisibleLayout {
	public final static String NAME = "pref_drag_resize";
	public final static boolean DEFAULT = true;

	public SwitchDragResize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }
	public SwitchDragResize(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
	public SwitchDragResize(Context context, AttributeSet attrs) { super(context, attrs); init(); }
	public SwitchDragResize(Context context) { super(context); init(); }

	private void init() {
		setKey(NAME);
		setDefaultValue(DEFAULT);
		setTitle(R.string.pref_drag_resize);
		setSummary(R.string.pref_drag_resize_summary);
		setChecked(getPersistedBoolean(DEFAULT));
	}
}
