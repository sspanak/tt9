package io.github.sspanak.tt9.preferences.screens.main;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.preferences.screens.backup.BackupScreen;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class PreferenceBackupScreen extends ScreenPreference {
	public static final String NAME = "screen_backup";

	@Nullable private PreferencesActivity activity;

	public PreferenceBackupScreen(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	public PreferenceBackupScreen(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public PreferenceBackupScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PreferenceBackupScreen(@NonNull Context context) {
		super(context);
		init();
	}

	private void init() {
		setKey(NAME);
		if (DeviceInfo.AT_LEAST_ANDROID_8) {
			setTitle(R.string.pref_category_backup);
		} else {
			setVisible(false);
		}
	}

	@Override
	protected void onClick() {
		if (activity != null && !activity.getSettings().getDemoMode()) {
			activity.displayScreen(BackupScreen.NAME);
			super.onClick();
		}
	}

	@Override
	public int getLargeLayout() {
		return R.layout.pref_default_large;
	}

	public void populate(@Nullable PreferencesActivity activity) {
		this.activity = activity;
	}
}
