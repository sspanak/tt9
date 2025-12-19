package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class SwitchArrowsUpDown extends SwitchWhenUIVisible {
	public final static String NAME = "pref_arrows_up_down";
	public final static boolean DEFAULT = false;

	public SwitchArrowsUpDown(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }
	public SwitchArrowsUpDown(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
	public SwitchArrowsUpDown(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
	public SwitchArrowsUpDown(@NonNull Context context) { super(context); init(); }

	private void init() {
		setKey(NAME);
		setTitle(R.string.pref_arrows_up_down);
		setSummary(R.string.pref_arrows_up_down_summary);
		setChecked(getPersistedBoolean(DEFAULT));
	}

	@Override
	public void onLayoutChange(int mainViewLayout) {
		setVisible(mainViewLayout == SettingsStore.LAYOUT_CLASSIC || mainViewLayout == SettingsStore.LAYOUT_NUMPAD);
	}
}
