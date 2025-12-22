package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SwitchDragResize extends SwitchWhenLargeTouchscreenLayout {
	public final static String NAME = "pref_drag_resize";
	public final static boolean DEFAULT = true;

	public SwitchDragResize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public SwitchDragResize(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public SwitchDragResize(Context context, AttributeSet attrs) { super(context, attrs); }
	public SwitchDragResize(Context context) { super(context); }

	@Override protected String getName() { return NAME; }
	@Override protected boolean getDefault() { return DEFAULT; }
	@Override protected int getTitleResId() { return R.string.pref_drag_resize; }
	@Override protected int getSummaryResId() { return R.string.pref_drag_resize_summary; }
}
