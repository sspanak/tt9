package io.github.sspanak.tt9.preferences.screens.backup;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.customWords.SettingsExporter;
import io.github.sspanak.tt9.db.customWords.SettingsImporter;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.ScreenPreference;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class PreferenceExportSettings extends ScreenPreference {
	public static final String NAME = "export_settings";

	@Nullable private PreferencesActivity activity;
	@Nullable private SettingsExporter exporter;

	public PreferenceExportSettings(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(context); }
	public PreferenceExportSettings(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }
	public PreferenceExportSettings(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public PreferenceExportSettings(@NonNull Context context) { super(context); init(context); }


	private void init(@NonNull Context context) {
		setKey(NAME);
		setTitle(R.string.backup_export);

		if (DeviceInfo.AT_LEAST_ANDROID_8) {
			exporter = new SettingsExporter();
			exporter.setFailureHandler(this::onFailure);
			exporter.setSuccessHandler(this::onSuccess);
			setSummary(context.getString(
				R.string.backup_export_summary,
				SettingsImporter.FILE_EXTENSION,
				exporter.getOutputDir()
			));
		}
	}

	@Override
	public int getLargeLayout() {
		return R.layout.pref_default_large;
	}


	@Override
	protected void onClick() {
		if (activity == null || exporter == null || !DeviceInfo.AT_LEAST_ANDROID_8) {
			return;
		}

		super.onClick();
		exporter.run(activity);
	}


	private void onFailure() {
		if (activity != null) {
			activity.runOnUiThread(() -> {
				if (activity != null) {
					setSummary(activity.getString(R.string.backup_export_failed));
				}
			});
		}
	}


	private void onSuccess(String s) {
		if (activity != null) {
			activity.runOnUiThread(() -> {
				if (activity != null && exporter != null && DeviceInfo.AT_LEAST_ANDROID_8) {
					setSummary(activity.getString(R.string.backup_export_success, exporter.getOutputFileName()));
				}
			});
		}
	}


	public PreferenceExportSettings populate(@NonNull PreferencesActivity activity) {
		this.activity = activity;
		setEnabled(!activity.getSettings().getDemoMode());
		return this;
	}
}
