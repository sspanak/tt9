package io.github.sspanak.tt9.preferences.screens.hotkeys;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PreferenceBackspaceHotkey extends PreferenceHotkey {
	public PreferenceBackspaceHotkey(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }
	public PreferenceBackspaceHotkey(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
	public PreferenceBackspaceHotkey(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public PreferenceBackspaceHotkey(@NonNull Context context) { super(context); }

	@Override
	protected boolean onAssign(DialogInterface dialog, int keyCode) {
		// backspace works both when pressed short and long,
		// so separate "hold" and "not hold" options for it make no sense
		return super.onAssign(dialog, Math.abs(keyCode));
	}
}
